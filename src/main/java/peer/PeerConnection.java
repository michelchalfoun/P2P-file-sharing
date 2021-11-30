package peer;

import messages.HandshakeMessage;
import messages.Message;
import messages.MessageFactory;
import messages.MessageType;
import messages.payload.BitfieldPayload;
import messages.payload.PayloadFactory;
import util.AtomicReferenceArrayHelper;

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
    private final boolean hasSentHandshakeMessage;
    private boolean hasSentBitfieldMessage;
    private boolean isHandshakeDone;

    private final AtomicReferenceArray<Boolean> pieces;
    private Map<Integer, AtomicReferenceArray> bitfieldsByNeighborID;

    private Map<Integer, Boolean> interestedByNeighborID;

    private final MessageFactory messageFactory;
    private final PayloadFactory payloadFactory;

    public PeerConnection(
            final int peerID,
            final Socket neighborSocket,
            final AtomicReferenceArray<Boolean> pieces,
            final Map<Integer, AtomicReferenceArray> bitfieldsByNeighborID,
            final Map<Integer, Boolean> interestedByNeighborID) {

        this(peerID, neighborSocket, false, pieces, bitfieldsByNeighborID, interestedByNeighborID);
        try {
            outputStream = new ObjectOutputStream(connection.getOutputStream());
            outputStream.flush();
            inputStream = new ObjectInputStream(connection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PeerConnection(
            final int peerID,
            final Socket neighborSocket,
            final boolean hasSentHandshakeMessage,
            final AtomicReferenceArray<Boolean> pieces,
            final Map<Integer, AtomicReferenceArray> bitfieldsByNeighborID,
            final Map<Integer, Boolean> interestedByNeighborID) {
        connection = neighborSocket;
        messageFactory = new MessageFactory();
        payloadFactory = new PayloadFactory();
        hasSentBitfieldMessage = false;
        isHandshakeDone = false;
        this.peerID = peerID;
        this.hasSentHandshakeMessage = hasSentHandshakeMessage;
        this.bitfieldsByNeighborID = bitfieldsByNeighborID;
        this.interestedByNeighborID = interestedByNeighborID;
        this.pieces = pieces;
    }

    public PeerConnection(
            final int peerID,
            final Socket neighborSocket,
            final boolean hasSentHandshakeMessage,
            final AtomicReferenceArray<Boolean> pieces,
            final Map<Integer, AtomicReferenceArray> bitfieldsByNeighborID,
            final Map<Integer, Boolean> interestedByNeighborID,
            final ObjectOutputStream outputStream,
            final ObjectInputStream inputStream) {
        this(peerID, neighborSocket, hasSentHandshakeMessage, pieces, bitfieldsByNeighborID, interestedByNeighborID);
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

                        // check handshake is fine
                        System.out.println("Received handshake message: " + handshakeMessage);

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

        // All other type of messages
        switch (message.getMessageType()) {
            case CHOKE:

                break;
            case UNCHOKE:

                break;
            case INTERESTED:
                System.out.println("Received Interested.");
                interestedByNeighborID.put(peerID, true);
                break;
            case NOT_INTERESTED:
                System.out.println("Received Not Interested.");
                interestedByNeighborID.put(peerID, false);
                break;
            case HAVE:
                final int pieceID = payloadFactory.createHavePayload(message).getPieceID();
                final AtomicReferenceArray bitfield = bitfieldsByNeighborID.get(peerID);
                bitfield.set(pieceID, true);

                // TODO
                // Check if it should send a not interested message
                if (AtomicReferenceArrayHelper.isEqual(bitfield, pieces)) {
                    sendIntentMessage(true);
                }
                break;
            case BITFIELD:
                System.out.println("Received bitfield from " + peerID);
                final BitfieldPayload payload = payloadFactory.createBitfieldPayload(message);
                bitfieldsByNeighborID.put(peerID, payload.getPieces());
                sendIntentMessage(!AtomicReferenceArrayHelper.isEqual(payload.getPieces(), pieces));
                if (!hasSentBitfieldMessage) {
                    sendBitfieldPayload();
                }
                break;
            case REQUEST:
                break;
            case PIECE:
                break;
        }
    }

    // Send message with interested/uninterested intent
    private void sendIntentMessage(final boolean interested) {
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


    // Send bitfield payload
    private void sendBitfieldPayload() {
        final BitfieldPayload bitfieldPayload = payloadFactory.createBitfieldPayload(pieces);
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
