package peer;

import messages.HandshakeMessage;
import messages.Message;
import messages.MessageFactory;
import messages.MessageType;
import messages.payload.impl.BitfieldPayload;
import messages.payload.PayloadFactory;
import messages.payload.impl.PiecePayload;
import pieces.PieceManager;
import util.AtomicReferenceArrayHelper;
import neighbor.Neighbor;
import util.RandomMissingPieceGenerator;
import logging.Logging;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

/** Listener thread that has the logic to communicate with other peers */
public class PeerConnection extends Thread {
    private Socket connection;
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

    private final AtomicReferenceArray<Boolean> pieces;
    private final AtomicInteger numberOfPiecesDownloaded;
    private final Map<Integer, Neighbor> neighborData;

    private final MessageFactory messageFactory;
    private final PayloadFactory payloadFactory;
    private final PieceManager pieceManager;

    private Set<Integer> peerRequestedPieces;

    public PeerConnection(
            final int peerID,
            final Socket neighborSocket,
            final boolean hasSentHandshakeMessage,
            final AtomicReferenceArray<Boolean> pieces,
            final Map<Integer, Neighbor> neighborData,
            final PieceManager pieceManager,
            final Set<Integer> peerRequestedPieces,
            final AtomicInteger numberOfPiecesDownloaded) {
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
        this.numberOfPiecesDownloaded = numberOfPiecesDownloaded;
    }

    // Constructor for host listening from a connection
    public PeerConnection(
            final int peerID,
            final Socket neighborSocket,
            final AtomicReferenceArray<Boolean> pieces,
            final Map<Integer, Neighbor> neighborData,
            final PieceManager pieceManager,
            final Set<Integer> peerRequestedPieces,
            final AtomicInteger numberOfPiecesDownloaded) {

        this(peerID, neighborSocket, false, pieces, neighborData, pieceManager, peerRequestedPieces, numberOfPiecesDownloaded);
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
            final AtomicReferenceArray<Boolean> pieces,
            final Map<Integer, Neighbor> neighborData,
            final ObjectOutputStream outputStream,
            final ObjectInputStream inputStream,
            final PieceManager pieceManager,
            final Set<Integer> peerRequestedPieces,
    final AtomicInteger numberOfPiecesDownloaded) {
        this(peerID, neighborSocket, hasSentHandshakeMessage, pieces, neighborData, pieceManager, peerRequestedPieces, numberOfPiecesDownloaded);
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    public void run() {
        try {
            try {
                while (true) {
                    // TODO 'receives a handshake message from peer B and checks whether peer B is the right neighbor'
                    if (!isHandshakeDone) {
                        // Handshake step
                        final HandshakeMessage handshakeMessage =
                                (HandshakeMessage) inputStream.readObject();

                        neighborID = handshakeMessage.getPeerID();
                        logger.TCP_receive(neighborID, peerID);
                        System.out.println("Received handshake message: " + handshakeMessage);

                        final Neighbor neighbor = new Neighbor(connection, neighborID, outputStream, new AtomicReferenceArray<>(pieces.length()));
                        neighborData.put(neighborID, neighbor);

                        if (!hasSentHandshakeMessage) {
                            final HandshakeMessage responseHandshakeMessage =
                                    new HandshakeMessage(peerID);
                            neighborData.get(neighborID).sendMessageInOutputStream(responseHandshakeMessage);
                        } else {
                            sendBitfieldMessage();
                        }
                        isHandshakeDone = true;
                    } else {
                        // Other messages
                        processMessage();
                    }
                }
            } catch (final ClassNotFoundException classnot) {
                System.err.println("Data received in unknown format");
            }
        } catch (final IOException ioException) {
            ioException.printStackTrace();
            System.out.println("Disconnect with neighbor peer");
        } finally {
            try {
                inputStream.close();
                outputStream.close();
            } catch (final IOException ioException) {
                ioException.printStackTrace();
                System.out.println("Disconnect with neighbor peer!");
            }
        }
    }

    private void processMessage() throws IOException, ClassNotFoundException {
        final Message message = (Message) inputStream.readObject();

        switch (message.getMessageType()) {
            case CHOKE:
                neighborConnectionChoked = true;
                logger.unchoking(peerID, neighborID);
                break;
            case UNCHOKE:
                neighborConnectionChoked = false;
                logger.choking(peerID, neighborID);
                requestRandomPiece();
                break;
            case INTERESTED:
                neighborData.get(neighborID).setInterested(true);
                logger.receiveInterestedMsg(peerID, neighborID);
                break;
            case NOT_INTERESTED:
                neighborData.get(neighborID).setInterested(false);
                logger.receiveNotInterestedMsg(peerID, neighborID);
                break;
            case HAVE:
                final int obtainedPieceID = payloadFactory.createPieceIndexPayload(message).getPieceID();
                final AtomicReferenceArray bitfield = neighborData.get(neighborID).getBitfield();
                bitfield.set(obtainedPieceID, true);
                logger.receiveHaveMsg(peerID, neighborID, obtainedPieceID);
                // TODO
//                 Check if it should send a not interested message
                if (AtomicReferenceArrayHelper.isInterested(pieces, bitfield)) {
                    sendIntentMessage(true);
                }
                break;
            case BITFIELD:
                final BitfieldPayload payload = payloadFactory.createBitfieldPayload(message, pieces.length());
                System.out.println("Received bitfield from " + neighborID + " " + payload);

                neighborData.get(neighborID).setBitfield(payload.getPieces());
                sendIntentMessage(AtomicReferenceArrayHelper.isInterested(pieces, payload.getPieces()));

                if (!hasSentBitfieldMessage) {
                    sendBitfieldMessage();
                }
                break;
            case REQUEST:
                final int requestedPieceID = payloadFactory.createPieceIndexPayload(message).getPieceID();
                System.out.println("Received request message for " + requestedPieceID + " from neighbor " + neighborID + " at " + System.currentTimeMillis());

                // TODO
//                Even though peer A sends a ‘request’ message to peer B, it may not receive a ‘piece’
//                message corresponding to it. This situation happens when peer B re-determines
//                preferred neighbors or optimistically unchoked a neighbor and peer A is choked as the
//                result before peer B responds to peer A. Your program should consider this case.
                if (!neighborData.get(neighborID).isChoked()) {
                    logger.custom("Received request message for " + requestedPieceID + " from neighbor " + neighborID);
                    sendPiece(requestedPieceID);
                }
                break;
            case PIECE:
                final PiecePayload piecePayload = payloadFactory.createPiecePayload(message);
                pieceManager.savePiece(piecePayload.getPieceID(), piecePayload.getPayload());
                pieces.set(piecePayload.getPieceID(), true);

                neighborData.get(neighborID).addNumberOfDownloadedBytes(piecePayload.getPayload().length);
                System.out.println("Obtained piece " + piecePayload.getPieceID() + " from " + neighborID);
                broadcastReceivedPiece(piecePayload.getPieceID());

                logger.downloadingPiece(peerID, neighborID, piecePayload.getPieceID(), numberOfPiecesDownloaded.get());

                // Has the entire file
                if (hasAllPieces()) {
                    pieceManager.consolidatePiecesIntoFile();
                    // TODO: should it send uninterested?
                    sendIntentMessage(false);
                    logger.completionOfDownload(peerID);
                } else if (!neighborConnectionChoked) { // Request another piece if unchoked
                    requestRandomPiece();
                }
                numberOfPiecesDownloaded.getAndIncrement();
                break;
        }
    }

//    private int countNumberOfPieces() {
//        int numberOfPieces = 0;
//        for (int index = 0; index < pieces.length(); index++) {
//            if (pieces.get(index)) numberOfPieces++;
//        }
//        return numberOfPieces;
//    }

    private void requestRandomPiece() {
        final RandomMissingPieceGenerator randomGenerator =
                new RandomMissingPieceGenerator(pieces, neighborData.get(neighborID).getBitfield(), peerRequestedPieces);
        int pieceIndex = randomGenerator.getRandomPiece();

        if(pieceIndex == -1){
            // TODO: Maybe not what we need need it to do?
            sendIntentMessage(false);
        } else {
            sendRequestMessage(pieceIndex);
        }
    }

    private boolean hasAllPieces() {
        for (int index = 0; index < pieces.length(); index++) {
            if (!pieces.get(index)) {
                return false;
            }
        }
        return true;
    }

    private void broadcastReceivedPiece(final int pieceID) {
        neighborData.values().forEach(neighbor -> sendHaveMessage(pieceID, neighbor));
    }

    private void sendPiece(final int pieceID) {
        System.out.println("Sending piece to neighbor " + neighborID + " at " + System.currentTimeMillis());
        final byte[] pieceBytes = pieceManager.getPiece(pieceID);
        final Message pieceMessage =
                messageFactory.createMessage(
                        payloadFactory.createPiecePayload(pieceID, pieceBytes), MessageType.PIECE);
        neighborData.get(neighborID).sendMessageInOutputStream(pieceMessage);
    }

    private void sendHaveMessage(final int pieceID, final Neighbor targetNeighbor) {
        System.out.println("Sending have message for pieceID " + pieceID + " for neighbor " + targetNeighbor.getPeerID()+ " at " + System.currentTimeMillis());
        final Message haveMessage =
                messageFactory.createMessage(payloadFactory.createPieceIndexPayload(pieceID), MessageType.HAVE);
        targetNeighbor.sendMessageInOutputStream(haveMessage);
        System.out.println(peerID);
    }

    // Send message with interested/uninterested intent
    private void sendIntentMessage(final boolean interested) {
        System.out.println("Sent interest of " +interested + " to peer " + neighborID + " at " + System.currentTimeMillis());
        final Message notInterested =
                new Message(
                        interested
                                ? MessageType.INTERESTED.getValue()
                                : MessageType.NOT_INTERESTED.getValue());
        neighborData.get(neighborID).sendMessageInOutputStream(notInterested);
    }

    private void sendRequestMessage(final int pieceIndex) {
        System.out.println("Sending request to " + neighborID + " at " + System.currentTimeMillis());
        final Message request =
                messageFactory.createMessage(payloadFactory.createPieceIndexPayload(pieceIndex), MessageType.REQUEST);
        neighborData.get(neighborID).sendMessageInOutputStream(request);
        peerRequestedPieces.add(pieceIndex);
    }


    // Send bitfield payload
    private void sendBitfieldMessage() {
        final BitfieldPayload bitfieldPayload = payloadFactory.createBitfieldPayload(pieces);
        System.out.println("Sending bitfield payload to peer " + neighborID);
        final Message bitfieldMessage =
                messageFactory.createMessage(bitfieldPayload, MessageType.BITFIELD);

        neighborData.get(neighborID).sendMessageInOutputStream(bitfieldMessage);
        hasSentBitfieldMessage = true;
        System.out.println("Sent bitfield message: " + bitfieldMessage);
    }
}
