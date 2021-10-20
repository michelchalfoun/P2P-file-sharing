package peer;

import config.CommonConfig;
import config.PeerInfoConfig;
import messages.HandshakeMessage;

import java.net.*;
import java.io.*;
import java.util.*;

public class Peer {
    private final int peerID;

    private final CommonConfig commonConfig;
    private final PeerInfoConfig peerInfoConfig;

    private final PeerMetadata metadata;

    private final Map<Integer, Socket> socketConnectionsByNeighborID;

    private final Set<Integer> neighborsContacted;

    private ServerSocket listenerSocket;

    public Peer(final int peerID) {
        this.peerID = peerID;

        commonConfig = new CommonConfig();
        peerInfoConfig = new PeerInfoConfig();
        metadata = peerInfoConfig.getPeerInfo(peerID);

        socketConnectionsByNeighborID = new HashMap<>();
        neighborsContacted = new HashSet<>();

        try {
            listenerSocket = new ServerSocket(metadata.getListeningPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendHandshake(final int neighborPeerID, final PeerMetadata metadata) {
        final HandshakeMessage handshakeMessage = new HandshakeMessage(peerID);
        try {
            final Socket neighborSocket =
                    new Socket(metadata.getHostName(), metadata.getListeningPort());
            final ObjectOutputStream outputStream =
                    new ObjectOutputStream(neighborSocket.getOutputStream());

            neighborsContacted.add(neighborPeerID);

            outputStream.flush();
            outputStream.writeObject(handshakeMessage);

            System.out.println("Sent handshake message to: " + handshakeMessage);

            outputStream.flush();
            socketConnectionsByNeighborID.put(neighborPeerID, neighborSocket);

            ObjectInputStream in = new ObjectInputStream(neighborSocket.getInputStream());
            try {
                final HandshakeMessage responseMessage = (HandshakeMessage) in.readObject();

                if (responseMessage.getPeerID() == neighborPeerID){
                    System.out.println("Successful handshake with " + neighborPeerID + '.');
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenForConnections() {
        try {
            while (true) {
                Socket socket = listenerSocket.accept();
                new PeerConnection(peerID, socket, neighborsContacted).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                listenerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void run() {
        peerInfoConfig
                .getPrevPeers(peerID)
                .forEach((neighborPeerID, metadata) -> sendHandshake(neighborPeerID, metadata));



        listenForConnections();
    }

    public static void main(String args[]) {
        Peer client = new Peer(Integer.parseInt(args[0]));
        System.out.println("Process "+ "\u001B[31m" + args[0] + "\u001B[0m" + " running.");
        client.run();
    }
}
