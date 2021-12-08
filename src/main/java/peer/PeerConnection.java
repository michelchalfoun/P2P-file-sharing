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
    private boolean neighborConnectionChoked;

    private final Pieces pieces;
    private final NeighborDataWrapper neighborData;

    private final MessageFactory messageFactory;
    private final PayloadFactory payloadFactory;
    private final PieceManager pieceManager;

    private final Set<Integer> peerRequestedPieces;

    private final Map<Integer, Boolean> isInterested;

    private final int numberOfNeighbors;
    private final AtomicBoolean isRunning;

    // Parent constructor
    public PeerConnection(
            final int peerID,
            final Socket neighborSocket,
            final boolean hasSentHandshakeMessage,
            final Pieces pieces,
            final NeighborDataWrapper neighborData,
            final PieceManager pieceManager,
            final Set<Integer> peerRequestedPieces,
            final int numberOfNeighbors,
            final AtomicBoolean isRunning,
            final Map<Integer, Boolean> isInterested) {
        connection = neighborSocket;
        messageFactory = new MessageFactory();
        payloadFactory = new PayloadFactory();
        hasSentBitfieldMessage = false;
        isHandshakeDone = false;
        neighborConnectionChoked = true;
        logger = Logging.getInstance();
        this.isInterested = isInterested;
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
            final AtomicBoolean isRunning,
            final Map<Integer, Boolean> isInterested) {

        this(peerID, neighborSocket, false, pieces, neighborData, pieceManager, peerRequestedPieces, numberOfNeighbors, isRunning, isInterested);
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
            final AtomicBoolean isRunning,
            final Map<Integer, Boolean> isInterested) {
        this(peerID, neighborSocket, hasSentHandshakeMessage, pieces, neighborData, pieceManager, peerRequestedPieces, numberOfNeighbors, isRunning, isInterested);
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    public void run() {
        while (isRunning.get()) {

            // Initial connection
            if (!isHandshakeDone) {
                try {
                    final HandshakeMessage handshakeMessage =
                            (HandshakeMessage) inputStream.readObject();

                    neighborID = handshakeMessage.getPeerID();
                    logger.TCP_receive(peerID, neighborID);

                    final Neighbor neighbor = new Neighbor(connection, neighborID, inputStream, outputStream, pieces.getNumberOfPieces());
                    neighborData.getNeighborData().put(neighborID, neighbor);

                    //Handshake response
                    if (!hasSentHandshakeMessage) {
                        final HandshakeMessage responseHandshakeMessage =
                                new HandshakeMessage(peerID);
                        neighborData.getNeighborData().get(neighborID).sendMessageInOutputStream(responseHandshakeMessage);
                    } else {
                        sendBitfieldMessage();
                    }
                    isHandshakeDone = true;
                }
                catch (IOException e) { }
                catch (ClassNotFoundException e) { }
            } else {

                // Other messages
                try {
                    processMessage();
                }
                catch (ClassNotFoundException e) { }
                catch (IOException e) { }
            }
        }
    }

    private void processMessage() throws IOException, ClassNotFoundException {

        // Guard to stop processing messages
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

                // Thread safe setting of neighbor's bitfield (atomic operation)
                if (neighborPieces.hasAndSet(obtainedPieceID) != -1) {
                    logger.receiveHaveMsg(peerID, neighborID, obtainedPieceID);

                    // Checks if consolidation of file is now possible and executes it
                    checkAndConsolidate();
                    sendIntentMessage(pieces.isInterested(neighborPieces));
                }
                break;

            case BITFIELD:
                final BitfieldPayload payload = payloadFactory.createBitfieldPayload(message, pieces.getNumberOfPieces());

                // Initialize bitfield of neighbor
                neighborData.getNeighborData().get(neighborID).setPieces(payload.getPieces());
                sendIntentMessage(pieces.isInterested(payload.getPieces()));

                // Coordinate with handshake process to prevent endless loops of handshakes/bitfield messages
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

                    // Keeps track of downloaded pieces to check completion
                    neighborData.getNeighborData().get(neighborID).addNumberOfDownloadedBytes(piecePayload.getPayload().length);
                    logger.downloadingPiece(peerID, neighborID, piecePayload.getPieceID(), numberOfPiecesDownloaded);

                    if (pieceManager.hasAllPieces()) {
                        sendIntentMessage(false);
                        logger.completionOfDownload(peerID);
                    }

                    broadcastReceivedPiece(piecePayload.getPieceID());

                    // Check if file can now be consolidated and executes it
                    checkAndConsolidate();
                }

                // Request another piece if unchoked
                if (!pieceManager.hasAllPieces() && !neighborConnectionChoked) {
                    requestRandomPiece();
                }
                break;
        }
    }

    private void requestRandomPiece() {

        // Generate random piece index
        int pieceIndex = pieces.getRandomPiece(neighborData.getNeighborData().get(neighborID).getPieces());

        if (pieceIndex == -1) {

            // Send 'not interested' if no pieces interests current peer
            sendIntentMessage(false);

        } else {
            sendRequestMessage(pieceIndex);
        }
    }

    private void sendPiece(final int pieceID) {
        final byte[] pieceBytes = pieceManager.getPiece(pieceID);
        final Message pieceMessage =
                messageFactory.createMessage(
                        payloadFactory.createPiecePayload(pieceID, pieceBytes), MessageType.PIECE);
        neighborData.getNeighborData().get(neighborID).sendMessageInOutputStream(pieceMessage);
    }

    private void broadcastReceivedPiece(final int pieceID) {

        // Send have to all neighbors
        neighborData.getNeighborData().values().forEach(neighbor -> sendHaveMessage(pieceID, neighbor));
    }

    private void sendHaveMessage(final int pieceID, final Neighbor targetNeighbor) {
        final Message haveMessage =
                messageFactory.createMessage(payloadFactory.createPieceIndexPayload(pieceID), MessageType.HAVE);

        if (!isRunning.get()) return;

        targetNeighbor.sendMessageInOutputStream(haveMessage);
    }

    // Send message with interested/uninterested intent
    private void sendIntentMessage(final boolean interested) {

        // Guard to prevent sending duplicate interested message
        if (isInterested.get(neighborID) == interested) return;

        final Message intentMessage =
                new Message(
                        interested
                                ? MessageType.INTERESTED.getValue()
                                : MessageType.NOT_INTERESTED.getValue());

        if (!isRunning.get()) return;

        neighborData.getNeighborData().get(neighborID).sendMessageInOutputStream(intentMessage);
        isInterested.put(neighborID, interested);
    }

    private void sendRequestMessage(final int pieceIndex) {
        final Message request =
                messageFactory.createMessage(payloadFactory.createPieceIndexPayload(pieceIndex), MessageType.REQUEST);
        neighborData.getNeighborData().get(neighborID).sendMessageInOutputStream(request);

        // Add to requested pieces to not send duplicate requests
        peerRequestedPieces.add(pieceIndex);
    }


    private void sendBitfieldMessage() {
        final BitfieldPayload bitfieldPayload = payloadFactory.createBitfieldPayload(pieces);
        final Message bitfieldMessage =
                messageFactory.createMessage(bitfieldPayload, MessageType.BITFIELD);

        neighborData.getNeighborData().get(neighborID).sendMessageInOutputStream(bitfieldMessage);

        // Coordinates with handshake/bitfield process
        hasSentBitfieldMessage = true;
    }

    private void checkAndConsolidate(){

        // Prevents early consolidation before having connected to ALL other neighbors
        if (neighborData.getNeighborData().size() != numberOfNeighbors) return;

        // Checks if current peer has all pieces
        boolean allFilesDownloaded = pieces.getNumberOfPiecesDownloaded() == pieces.getNumberOfPieces();

        // If any neighbor doesn't have all pieces, prevent consolidation
        if (allFilesDownloaded) {
            for (Map.Entry<Integer, Neighbor> neighbor : neighborData.getNeighborData().entrySet()) {
                if (neighbor.getValue().getPieces().getNumberOfPiecesDownloaded()
                        != neighbor.getValue().getPieces().getNumberOfPieces()) {
                    allFilesDownloaded = false;
                }
            }
        }

        // Only consolidate when EVERYONE has ALL pieces
        if (allFilesDownloaded) {
            pieceManager.consolidatePiecesIntoFile(neighborData);
        }
    }
}
