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

            System.out.println("At this point: " + neighborsContacted);

            outputStream.flush();
            outputStream.writeObject(handshakeMessage);
            System.out.println("Peer " + peerID + " sent handshake message to " + neighborPeerID);
            outputStream.flush();
            socketConnectionsByNeighborID.put(neighborPeerID, neighborSocket);

//            new PeerConnection(peerID, socket, neighborsContacted).start();

            ObjectInputStream in = new ObjectInputStream(neighborSocket.getInputStream());
            while(true) {
                try {
                    final HandshakeMessage d = (HandshakeMessage) in.readObject();
                    System.out.println("Received response message: " + handshakeMessage);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

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
        client.run();
    }
}
