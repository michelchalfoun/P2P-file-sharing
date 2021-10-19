package peer;

import config.CommonConfig;
import config.PeerInfoConfig;
import messages.HandshakeMessage;

import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Peer {
    private final int peerID;

    private final CommonConfig commonConfig;
    private final PeerInfoConfig peerInfoConfig;

    private final PeerMetadata metadata;

    private final Map<Integer, Socket> socketConnectionsByNeighborID;

    private ServerSocket listenerSocket;

    public Peer(final int peerID) {
        this.peerID = peerID;

        commonConfig = new CommonConfig();
        peerInfoConfig = new PeerInfoConfig();
        metadata = peerInfoConfig.getPeerInfo(peerID);
        socketConnectionsByNeighborID = new HashMap<>();

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
            outputStream.flush();
            outputStream.writeObject(handshakeMessage);
            outputStream.flush();
            outputStream.close();
            socketConnectionsByNeighborID.put(neighborPeerID, neighborSocket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenForConnections() {
        try {
            while (true) {
                Socket socket = listenerSocket.accept();
                new PeerConnection(socket).start();
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
                .forEach(
                        (neighborPeerID, metadata) -> sendHandshake(neighborPeerID, metadata));

        listenForConnections();
    }

    public static void main(String args[]) {
        Peer client = new Peer(Integer.parseInt(args[0]));
        client.run();
    }
}
