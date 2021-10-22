package peer;

import messages.HandshakeMessage;
import messages.Message;
import messages.MessageType;
import messages.Payload.BitfieldPayload;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Set;

import java.util.concurrent.atomic.AtomicReferenceArray;

public class PeerConnection extends Thread {
    private Socket connection;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    private final int peerID;
    private Set<Integer> neighborsContacted;

    private final AtomicReferenceArray<Boolean> pieces;

    private boolean handshakeDone = false;

    public PeerConnection(
            final int peerID,
            final Socket neighborSocket,
            final Set<Integer> neighborsContacted,
            final AtomicReferenceArray<Boolean> pieces) {
        connection = neighborSocket;
        this.peerID = peerID;
        this.neighborsContacted = neighborsContacted;
        this.pieces = pieces;

        try {
            outputStream = new ObjectOutputStream(connection.getOutputStream());
            outputStream.flush();
            inputStream = new ObjectInputStream(connection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PeerConnection(final int peerID, final AtomicReferenceArray<Boolean> pieces) {
        this.peerID = peerID;
        this.pieces = pieces;
    }


    public PeerConnection(
            final int peerID,
            final Socket neighborSocket,
            final Set<Integer> neighborsContacted,
            final AtomicReferenceArray<Boolean> pieces,
            ObjectOutputStream outputStream,
            ObjectInputStream inputStream) {
        connection = neighborSocket;
        this.peerID = peerID;
        this.neighborsContacted = neighborsContacted;
        this.inputStream = inputStream;
        this.outputStream = outputStream;

        this.pieces = pieces;
    }

    public void run() {
        try {
            try {
                while (true) {
                    // Handshake step
                    if (!handshakeDone) {
                        final HandshakeMessage handshakeMessage =
                                (HandshakeMessage) inputStream.readObject();

                        // check handshake is fine
                        System.out.println("Received handshake message: " + handshakeMessage);

                        if (!neighborsContacted.contains(handshakeMessage.getPeerID())) {
                            HandshakeMessage responseHandshakeMessage =
                                    new HandshakeMessage(peerID);
                            outputStream.writeObject(responseHandshakeMessage);
                            System.out.println(
                                    "Sent handshake response message to: "
                                            + responseHandshakeMessage);
                            outputStream.flush();

                            sendBitfieldPayload();
                        } else {
                            sendBitfieldPayload();
                        }
                        handshakeDone = true;
                    } else {
                        final Message message = (Message) inputStream.readObject();
                        System.out.println(message);

                        switch (message.getMessageType()) {
                            case CHOKE:
                                break;
                            case UNCHOKE:
                                break;
                            case INTERESTED:
                                System.out.println("Received Interested.");
                                break;
                            case NOT_INTERESTED:
                                System.out.println("Received Not Interested.");
                                break;
                            case HAVE:
                                break;
                            case BITFIELD:
                                final BitfieldPayload payload =
                                        new BitfieldPayload(
                                                message.getMessageLength(),
                                                message.getMessagePayload());

                                boolean[] convertedPieces =
                                        convertAtomicReferenceArray(payload.getPieces());
                                boolean[] currentPieces = convertAtomicReferenceArray(pieces);

                                boolean isEqual = convertedPieces.equals(currentPieces);

                                sendIntentMessage(!isEqual);
                                break;
                            case REQUEST:
                                break;
                            case PIECE:
                                break;
                        }
                    }

                    // Other messages
                }
            } catch (ClassNotFoundException classnot) {
                System.err.println("Data received in unknown format");
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
            System.out.println("Disconnect with neighbor peer");
        } finally {
            try {
                inputStream.close();
                outputStream.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
                System.out.println("Disconnect with neighbor peer!");
            }
        }
    }

    private void sendIntentMessage(final boolean interested) {
        Message notInterested =
                new Message(
                        1,
                        interested
                                ? MessageType.INTERESTED.getValue()
                                : MessageType.NOT_INTERESTED.getValue(),
                        new byte[] {});
        try {
            outputStream.writeObject(notInterested);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean[] convertAtomicReferenceArray(
            AtomicReferenceArray<Boolean> atomicReferenceArray) {
        boolean[] array = new boolean[atomicReferenceArray.length()];
        for (int i = 0; i < atomicReferenceArray.length(); i++) {
            array[i] = atomicReferenceArray.get(i);
        }
        return array;
    }

    private void sendBitfieldPayload() {
        int messageLength = (int) Math.ceil(pieces.length() / 8) + 1;
        BitfieldPayload bitfieldPayload = new BitfieldPayload(pieces);
        byte[] bytePayload = bitfieldPayload.getPayload();

        Message bitfieldMessage =
                new Message(messageLength, MessageType.BITFIELD.getValue(), bytePayload);

        try {
            outputStream.writeObject(bitfieldMessage);
            outputStream.flush();
            System.out.println("Sent bitfield message: " + bitfieldMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    boolean comparePieces(final AtomicReferenceArray<Boolean> current, final AtomicReferenceArray<Boolean> received){
        boolean equal = true;
        for (int i = 0; i < current.length(); i++){
            if (current.get(i) != received.get(i)){
                if (!current.get(i)){
                    equal = false;
                }
            }
        }
        return equal;
    }

}
