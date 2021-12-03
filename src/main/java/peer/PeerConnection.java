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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

import java.util.concurrent.atomic.AtomicReferenceArray;

/** Listener thread that has the logic to communicate with other peers */
public class PeerConnection extends Thread {
    private Socket connection;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    private final int peerID;
    private int neighborID;

    private final boolean hasSentHandshakeMessage;
    private boolean hasSentBitfieldMessage;
    private boolean isHandshakeDone;
    // represents if the neighbor -> peer connection is choked (neighbor is not sending us pieces)
    private boolean neighborConnectionChoked;

    private final AtomicReferenceArray<Boolean> pieces;
    private final Map<Integer, Neighbor> neighborData;

    private final MessageFactory messageFactory;
    private final PayloadFactory payloadFactory;
    private final PieceManager pieceManager;

    public PeerConnection(
            final int peerID,
            final Socket neighborSocket,
            final boolean hasSentHandshakeMessage,
            final AtomicReferenceArray<Boolean> pieces,
            final Map<Integer, Neighbor> neighborData,
            final PieceManager pieceManager) {
        connection = neighborSocket;
        messageFactory = new MessageFactory();
        payloadFactory = new PayloadFactory();
        hasSentBitfieldMessage = false;
        isHandshakeDone = false;
        this.peerID = peerID;
        this.hasSentHandshakeMessage = hasSentHandshakeMessage;
        this.neighborData = neighborData;
        this.pieces = pieces;
        this.pieceManager = pieceManager;
    }

    // Constructor for host listening from a connection
    public PeerConnection(
            final int peerID,
            final Socket neighborSocket,
            final AtomicReferenceArray<Boolean> pieces,
            final Map<Integer, Neighbor> neighborData,
            final PieceManager pieceManager) {

        this(peerID, neighborSocket, false, pieces, neighborData, pieceManager);
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
            final PieceManager pieceManager) {
        this(peerID, neighborSocket, hasSentHandshakeMessage, pieces, neighborData, pieceManager);
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
                        System.out.println("Received handshake message: " + handshakeMessage);

                        final Neighbor neighbor = new Neighbor(connection, neighborID);
                        neighborData.put(neighborID, neighbor);
                        neighbor.setOutputStream(outputStream);

                        if (!hasSentHandshakeMessage) {
                            final HandshakeMessage responseHandshakeMessage =
                                    new HandshakeMessage(peerID);
                            outputStream.writeObject(responseHandshakeMessage);
                            System.out.println(
                                    "Sent handshake response message to: "
                                            + responseHandshakeMessage);
                            outputStream.flush();
                        } else {
                            sendBitfieldPayload();
                        }
                        // TODO
                        // After handshaking, peer A sends a ‘bitfield’ message to let peer
                        // know which file pieces it has. Peer B will also send its ‘bitfield’
                        // message to peer A, unless it has no pieces.
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
        System.out.println(message);

        switch (message.getMessageType()) {
            case CHOKE:
                neighborConnectionChoked = true;
                break;
            case UNCHOKE:
                neighborConnectionChoked = false;

                //TODO Request

                RandomMissingPieceGenerator randomGenerator =
                        new RandomMissingPieceGenerator(pieces, neighborData.get(neighborID).getBitfield());
                int pieceIndex = randomGenerator.getRandomPiece();

                if(pieceIndex == -1){
                    // TODO: Maybe not what we need need it to do?
                    sendIntentMessage(false);
                } else {
                    sendRequestMessage(pieceIndex);
                }
                break;
            case INTERESTED:
                System.out.println("Received Interested.");
                neighborData.get(neighborID).setInterested(true);
                break;
            case NOT_INTERESTED:
                System.out.println("Received Not Interested.");
                neighborData.get(neighborID).setInterested(false);
                break;
            case HAVE:
                final int obtainedPieceID = payloadFactory.createPieceIndexPayload(message).getPieceID();
                final AtomicReferenceArray bitfield = neighborData.get(neighborID).getBitfield();
                bitfield.set(obtainedPieceID, true);

                // TODO
//                 Check if it should send a not interested message
                if (AtomicReferenceArrayHelper.isInterested(bitfield, pieces)) {
                    sendIntentMessage(true);
                }
                break;
            case BITFIELD:
                final BitfieldPayload payload = payloadFactory.createBitfieldPayload(message, pieces.length());
                System.out.println("Received bitfield from " + neighborID + " " + payload);
                System.out.println("My bitfield is " + pieces);

                neighborData.get(neighborID).setBitfield(payload.getPieces());
                sendIntentMessage(AtomicReferenceArrayHelper.isInterested(pieces, payload.getPieces()));

                if (!hasSentBitfieldMessage) {
                    sendBitfieldPayload();
                }
                break;
            case REQUEST:
                final int requestedPieceID = payloadFactory.createPieceIndexPayload(message).getPieceID();
                System.out.println("Received request message for " + requestedPieceID);
                // TODO
//                Even though peer A sends a ‘request’ message to peer B, it may not receive a ‘piece’
//                message corresponding to it. This situation happens when peer B re-determines
//                preferred neighbors or optimistically unchoked a neighbor and peer A is choked as the
//                result before peer B responds to peer A. Your program should consider this case.
                if (!neighborData.get(neighborID).isChoked()) {
                    sendPiece(requestedPieceID);
                }
                break;
            case PIECE:
                final PiecePayload piecePayload = payloadFactory.createPiecePayload(message);
                pieceManager.savePiece(piecePayload.getPieceID(), piecePayload.getPayload());
                pieces.set(piecePayload.getPieceID(), true);
                neighborData.get(neighborID).addNumberOfDownloadedBytes(piecePayload.getPayload().length);
                // broadcastReceivedPiece(piecePayload.getPieceID());
                // TODO: send another request
                if (hasAllPieces()) {
                    pieceManager.consolidatePiecesIntoFile();
                }
                break;
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

    }

    private void sendPiece(final int pieceID) {
        final byte[] pieceBytes = pieceManager.getPiece(pieceID);
        final Message pieceMessage =
                messageFactory.createMessage(
                        payloadFactory.createPiecePayload(pieceID, pieceBytes), MessageType.PIECE);
        try {
            outputStream.writeObject(pieceMessage);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Send message with interested/uninterested intent
    private void sendIntentMessage(final boolean interested) {
        System.out.println("Sent interest of " +interested + " to peer " + neighborID);
        final Message notInterested =
                new Message(
                        interested
                                ? MessageType.INTERESTED.getValue()
                                : MessageType.NOT_INTERESTED.getValue());
        try {
            outputStream.writeObject(notInterested);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendRequestMessage(final int pieceIndex) {
        final Message request =
                messageFactory.createMessage(payloadFactory.createPieceIndexPayload(pieceIndex), MessageType.REQUEST);
        try {
            outputStream.writeObject(request);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Send bitfield payload
    private void sendBitfieldPayload() {
        final BitfieldPayload bitfieldPayload = payloadFactory.createBitfieldPayload(pieces);
        System.out.println("Sending bitfield payload to peer " + neighborID);
        final Message bitfieldMessage =
                messageFactory.createMessage(bitfieldPayload, MessageType.BITFIELD);

        try {
            outputStream.writeObject(bitfieldMessage);
            outputStream.flush();
            hasSentBitfieldMessage = true;
            System.out.println("Sent bitfield message: " + bitfieldMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
