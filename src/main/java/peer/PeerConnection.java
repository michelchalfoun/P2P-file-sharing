package peer;

import messages.HandshakeMessage;
import messages.Message;
import messages.MessageFactory;
import messages.MessageType;
import messages.payload.impl.BitfieldPayload;
import messages.payload.PayloadFactory;
import messages.payload.impl.PiecePayload;
import neighbor.NeighborDataWrapper;
import pieces.PieceManager;
import pieces.Pieces;
import neighbor.Neighbor;
import logging.Logging;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/** Listener thread that has the logic to communicate with other peers */
public class PeerConnection extends Thread {
    private final Socket connection;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    private final Logging logger;
    private final int peerID;
    private int neighborID;

    private final boolean hasSentHandshakeMessage;
    private boolean hasSentBitfieldMessage;
    private boolean isHandshakeDone;
    // represents if the neighbor -> peer connection is choked (neighbor is not sending us pieces)
    private boolean neighborConnectionChoked;

    private final Pieces pieces;
    private final NeighborDataWrapper neighborData;

    private final MessageFactory messageFactory;
    private final PayloadFactory payloadFactory;
    private final PieceManager pieceManager;

    private final Set<Integer> peerRequestedPieces;

    private final int numberOfNeighbors;
    private final AtomicBoolean isRunning;

    public PeerConnection(
            final int peerID,
            final Socket neighborSocket,
            final boolean hasSentHandshakeMessage,
            final Pieces pieces,
            final NeighborDataWrapper neighborData,
            final PieceManager pieceManager,
            final Set<Integer> peerRequestedPieces,
            final int numberOfNeighbors,
            final AtomicBoolean isRunning) {
        connection = neighborSocket;
        messageFactory = new MessageFactory();
        payloadFactory = new PayloadFactory();
        hasSentBitfieldMessage = false;
        isHandshakeDone = false;
        neighborConnectionChoked = true;
        logger = Logging.getInstance();
        this.peerID = peerID;
        this.hasSentHandshakeMessage = hasSentHandshakeMessage;
        this.neighborData = neighborData;
        this.pieces = pieces;
        this.pieceManager = pieceManager;
        this.peerRequestedPieces = peerRequestedPieces;
        this.numberOfNeighbors = numberOfNeighbors;
        this.isRunning = isRunning;
    }

    // Constructor for host listening from a connection
    public PeerConnection(
            final int peerID,
            final Socket neighborSocket,
            final Pieces pieces,
            final NeighborDataWrapper neighborData,
            final PieceManager pieceManager,
            final Set<Integer> peerRequestedPieces,
            final int numberOfNeighbors,
            final AtomicBoolean isRunning) {

        this(peerID, neighborSocket, false, pieces, neighborData, pieceManager, peerRequestedPieces, numberOfNeighbors, isRunning);
        try {
            outputStream = new ObjectOutputStream(connection.getOutputStream());
            outputStream.flush();
            inputStream = new ObjectInputStream(connection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Constructor for host initializing connection
    public PeerConnection(
            final int peerID,
            final Socket neighborSocket,
            final boolean hasSentHandshakeMessage,
            final Pieces pieces,
            final NeighborDataWrapper neighborData,
            final ObjectOutputStream outputStream,
            final ObjectInputStream inputStream,
            final PieceManager pieceManager,
            final Set<Integer> peerRequestedPieces,
            final int numberOfNeighbors,
            final AtomicBoolean isRunning) {
        this(peerID, neighborSocket, hasSentHandshakeMessage, pieces, neighborData, pieceManager, peerRequestedPieces, numberOfNeighbors, isRunning);
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    public void run() {
        System.out.println("A connection has for " + peerID);
        while (isRunning.get()) {
//             TODO 'receives a handshake message from peer B and checks whether peer B is the right neighbor'
            if (!isHandshakeDone) {
                try {
                    final HandshakeMessage handshakeMessage =
                            (HandshakeMessage) inputStream.readObject();

                    neighborID = handshakeMessage.getPeerID();
                    logger.TCP_receive(peerID, neighborID);

                    final Neighbor neighbor = new Neighbor(connection, neighborID, inputStream, outputStream, pieces.getNumberOfPieces());
                    neighborData.getNeighborData().put(neighborID, neighbor);

                    if (!hasSentHandshakeMessage) {
                        final HandshakeMessage responseHandshakeMessage =
                                new HandshakeMessage(peerID);
                        neighborData.getNeighborData().get(neighborID).sendMessageInOutputStream(responseHandshakeMessage);
                    } else {
                        sendBitfieldMessage();
                    }
                    isHandshakeDone = true;
                } catch (IOException e) {
//                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
//                    e.printStackTrace();
                }
            } else {
                // Other messages
                try {
                    processMessage();
                } catch (ClassNotFoundException e) {
//                    e.printStackTrace();
                } catch (IOException e) {
//                    e.printStackTrace();
                }
            }
        }
    }

    private void processMessage() throws IOException, ClassNotFoundException {
        if (!isRunning.get()) return;

        final Message message = (Message) inputStream.readObject();
        switch (message.getMessageType()) {
            case CHOKE:
                neighborConnectionChoked = true;
                logger.choking(peerID, neighborID);
                break;
            case UNCHOKE:
                neighborConnectionChoked = false;
                logger.unchoking(peerID, neighborID);
                requestRandomPiece();
                break;
            case INTERESTED:
                neighborData.getNeighborData().get(neighborID).setInterested(true);
                logger.receiveInterestedMsg(peerID, neighborID);
                break;
            case NOT_INTERESTED:
                neighborData.getNeighborData().get(neighborID).setInterested(false);
                logger.receiveNotInterestedMsg(peerID, neighborID);
                break;
            case HAVE:
                final int obtainedPieceID = payloadFactory.createPieceIndexPayload(message).getPieceID();
                final Pieces neighborPieces = neighborData.getNeighborData().get(neighborID).getPieces();

                if (neighborPieces.hasAndSet(obtainedPieceID) != -1) {
                    logger.receiveHaveMsg(peerID, neighborID, obtainedPieceID);
                    checkAndConsolidate();
                    sendIntentMessage(pieces.isInterested(neighborPieces));
                }
                break;
            case BITFIELD:
                final BitfieldPayload payload = payloadFactory.createBitfieldPayload(message, pieces.getNumberOfPieces());

                neighborData.getNeighborData().get(neighborID).setPieces(payload.getPieces());
                sendIntentMessage(pieces.isInterested(payload.getPieces()));

                if (!hasSentBitfieldMessage) {
                    sendBitfieldMessage();
                }
                break;
            case REQUEST:
                final int requestedPieceID = payloadFactory.createPieceIndexPayload(message).getPieceID();

                if (!neighborData.getNeighborData().get(neighborID).isChoked()) {
                    sendPiece(requestedPieceID);
                }
                break;
            case PIECE:
                final PiecePayload piecePayload = payloadFactory.createPiecePayload(message);

                final int numberOfPiecesDownloaded = pieces.hasAndSet(piecePayload.getPieceID());
                if (numberOfPiecesDownloaded != -1) {
                    pieceManager.savePiece(piecePayload.getPieceID(), piecePayload.getPayload());
                    neighborData.getNeighborData().get(neighborID).addNumberOfDownloadedBytes(piecePayload.getPayload().length);
                    logger.downloadingPiece(peerID, neighborID, piecePayload.getPieceID(), numberOfPiecesDownloaded);

                    if (pieceManager.hasAllPieces()) {
                        sendIntentMessage(false);
                        logger.completionOfDownload(peerID);
                    }

                    broadcastReceivedPiece(piecePayload.getPieceID());
                    checkAndConsolidate();
                }
                if (!pieceManager.hasAllPieces() && !neighborConnectionChoked) { // Request another piece if unchoked
                    requestRandomPiece();
                }
                break;
        }
    }

    private void requestRandomPiece() {
        int pieceIndex = pieces.getRandomPiece(neighborData.getNeighborData().get(neighborID).getPieces());

        if (pieceIndex == -1) {
            sendIntentMessage(false);
        } else {
            sendRequestMessage(pieceIndex);
        }
    }

    private void broadcastReceivedPiece(final int pieceID) {
        neighborData.getNeighborData().values().forEach(neighbor -> sendHaveMessage(pieceID, neighbor));
    }

    private void sendPiece(final int pieceID) {
        final byte[] pieceBytes = pieceManager.getPiece(pieceID);
        final Message pieceMessage =
                messageFactory.createMessage(
                        payloadFactory.createPiecePayload(pieceID, pieceBytes), MessageType.PIECE);
        neighborData.getNeighborData().get(neighborID).sendMessageInOutputStream(pieceMessage);
    }

    private void sendHaveMessage(final int pieceID, final Neighbor targetNeighbor) {
        final Message haveMessage =
                messageFactory.createMessage(payloadFactory.createPieceIndexPayload(pieceID), MessageType.HAVE);
        targetNeighbor.sendMessageInOutputStream(haveMessage);
    }

    // Send message with interested/uninterested intent
    private void sendIntentMessage(final boolean interested) {
        final Message notInterested =
                new Message(
                        interested
                                ? MessageType.INTERESTED.getValue()
                                : MessageType.NOT_INTERESTED.getValue());
        if (!isRunning.get()) return;
        neighborData.getNeighborData().get(neighborID).sendMessageInOutputStream(notInterested);
    }

    private void sendRequestMessage(final int pieceIndex) {
        final Message request =
                messageFactory.createMessage(payloadFactory.createPieceIndexPayload(pieceIndex), MessageType.REQUEST);
        neighborData.getNeighborData().get(neighborID).sendMessageInOutputStream(request);
        peerRequestedPieces.add(pieceIndex);
    }


    private void sendBitfieldMessage() {
        final BitfieldPayload bitfieldPayload = payloadFactory.createBitfieldPayload(pieces);
        final Message bitfieldMessage =
                messageFactory.createMessage(bitfieldPayload, MessageType.BITFIELD);

        neighborData.getNeighborData().get(neighborID).sendMessageInOutputStream(bitfieldMessage);
        hasSentBitfieldMessage = true;
    }

    private void checkAndConsolidate(){
        if (neighborData.getNeighborData().size() != numberOfNeighbors) return;
        boolean allFilesDownloaded = pieces.getNumberOfPiecesDownloaded() == pieces.getNumberOfPieces();
        if (allFilesDownloaded) {
            for (Map.Entry<Integer, Neighbor> neighbor : neighborData.getNeighborData().entrySet()) {
                if (neighbor.getValue().getPieces().getNumberOfPiecesDownloaded()
                        != neighbor.getValue().getPieces().getNumberOfPieces()) {
                    allFilesDownloaded = false;
                }
            }
        }

        if (allFilesDownloaded) {
            pieceManager.consolidatePiecesIntoFile(neighborData);
            int s = 0;
        }
    }
}
