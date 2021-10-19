package peer;

import messages.HandshakeMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class PeerConnection extends Thread {
    private String message;
    private String MESSAGE;
    private Socket connection;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public PeerConnection(final Socket neighborSocket) {
        connection = neighborSocket;
    }

    public void run() {
        try {
            out = new ObjectOutputStream(connection.getOutputStream());
            out.flush();
            in = new ObjectInputStream(connection.getInputStream());
            try {
                while (true) {
                    final HandshakeMessage hanshakeMessage = (HandshakeMessage) in.readObject();
                    System.out.println("Received message: " + hanshakeMessage);
                }
            } catch (ClassNotFoundException classnot) {
                System.err.println("Data received in unknown format");
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
            System.out.println("Disconnect with neighbor peer");
        } finally {
            try {
                in.close();
                out.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
                System.out.println("Disconnect with neighbor peer!");
            }
        }
    }
}
