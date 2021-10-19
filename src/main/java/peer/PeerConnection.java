package peer;

import messages.HandshakeMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Set;

public class PeerConnection extends Thread {
    private Socket connection;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    private final int peerID;
    private Set<Integer> neighborsContacted;

    public PeerConnection(final int peerID, final Socket neighborSocket, final Set<Integer> neighborsContacted) {
        connection = neighborSocket;
        this.peerID = peerID;
        this.neighborsContacted = neighborsContacted;
    }

    public void run() {
        try {
            outputStream = new ObjectOutputStream(connection.getOutputStream());
            outputStream.flush();
            inputStream = new ObjectInputStream(connection.getInputStream());
            try {
                while (true) {
                    final HandshakeMessage handshakeMessage = (HandshakeMessage) inputStream.readObject();

                    System.out.println("Received message: " + handshakeMessage);

                    System.out.println("Set: ");
                    System.out.println(neighborsContacted);
                    System.out.println("peerID: " + handshakeMessage.getPeerID());

                    if (!neighborsContacted.contains(handshakeMessage.getPeerID())) {
                        System.out.println("SEND THIS MESSAGE");
                        outputStream.writeObject(new HandshakeMessage(peerID));
                        outputStream.flush();
                    }
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
