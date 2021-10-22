package peer;

import config.CommonConfig;
import messages.HandshakeMessage;
import messages.Message;
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

    private CommonConfig commonConfig;

    private final int peerID;
    private Set<Integer> neighborsContacted;

    private final AtomicReferenceArray<Boolean> pieces;

    private boolean handshakeDone = false;

    public PeerConnection(final int peerID, final Socket neighborSocket, final Set<Integer> neighborsContacted, final AtomicReferenceArray<Boolean> pieces) {
        connection = neighborSocket;
        this.peerID = peerID;
        this.neighborsContacted = neighborsContacted;
        this.commonConfig = commonConfig;
        this.pieces = pieces;

        try {
            outputStream = new ObjectOutputStream(connection.getOutputStream());
            outputStream.flush();
            inputStream = new ObjectInputStream(connection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }}

    public PeerConnection(final int peerID, final Socket neighborSocket, final Set<Integer> neighborsContacted, final AtomicReferenceArray<Boolean> pieces,
                          ObjectOutputStream outputStream, ObjectInputStream inputStream) {
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
                        final HandshakeMessage handshakeMessage = (HandshakeMessage) inputStream.readObject();

                        // check handshake is fine

                        System.out.println("Received handshake message: " + handshakeMessage);

                        if (!neighborsContacted.contains(handshakeMessage.getPeerID())) {
                            HandshakeMessage responseHandshakeMessage = new HandshakeMessage(peerID);
                            outputStream.writeObject(responseHandshakeMessage);
                            System.out.println("Sent handshake response message to: " + responseHandshakeMessage);
                            outputStream.flush();

                        } else {
                            // send bitfield message
                            int messageLength = (int) Math.ceil(pieces.length() / 8) + 1;
                            BitfieldPayload bitfieldPayload = new BitfieldPayload(pieces);
                            byte[] bytePayload = bitfieldPayload.getPayload();
                            byte messageType = 5;

                            Message bitfieldMessage = new Message(messageLength, messageType, bytePayload);

                            outputStream.writeObject(bitfieldMessage);
                            System.out.println("Sent bitfield message: " + bitfieldMessage);
                        }
                        handshakeDone = true;
                    } else {
                        final Message message = (Message) inputStream.readObject();
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
}
