package peer;

import config.CommonConfig;
import config.PeerInfoConfig;

import java.net.*;
import java.io.*;

public class Peer {
    Socket requestSocket; // socket connect to the server
    ObjectOutputStream out; // stream write to the socket
    ObjectInputStream in; // stream read from the socket
    String message; // message send to the server
    String MESSAGE; // capitalized message read from the server

    private final CommonConfig commonConfig;
    private final PeerInfoConfig peerInfoConfig;

    private final PeerMetadata metadata;

    public Peer(final int peerID) {
        commonConfig = new CommonConfig();
        peerInfoConfig = new PeerInfoConfig();

        metadata = peerInfoConfig.getPeerInfo(peerID);
    }

    public void run() {
        try {
            requestSocket = new Socket("localhost", 8000);
            System.out.println("Connected to localhost in port 8000");

            out = new ObjectOutputStream(requestSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(requestSocket.getInputStream());

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                System.out.print("Hello, please input a sentence: ");
                message = bufferedReader.readLine();
                sendMessage(message);
                MESSAGE = (String) in.readObject();
                System.out.println("Received message: " + MESSAGE);
            }
        } catch (ConnectException e) {
            System.err.println("Connection refused. You need to initiate a server first.");
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found");
        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
                requestSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    void sendMessage(String msg) {
        try {
            // stream write the message
            out.writeObject(msg);
            out.flush();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public static void main(String args[]) {
        Peer client = new Peer(1003);
        client.run();
//        System.out.println(System.getProperty("user.dir"));
    }
}
