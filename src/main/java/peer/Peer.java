package peer;

import config.CommonConfig;
import config.PeerInfoConfig;
import messages.HandshakeMessage;
import logging.Logging;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * Main class that sets up the main Peer process (spins up all of the listener threads and keeps track of data)
 */
public class Peer
{
    private final int peerID;

    private final CommonConfig commonConfig;
    private final PeerInfoConfig peerInfoConfig;

    private final PeerMetadata metadata;

    private final Map<Integer, Socket> socketConnectionsByNeighborID;
    private final Map<Integer, AtomicReferenceArray> bitfieldsByNeighborID;
    private final Set<Integer> neighborsContacted;

    private ServerSocket listenerSocket;

    private AtomicReferenceArray<Boolean> pieceIndexes;
    private int numberOfPieces;

    private Logging logger;

    public Peer(final int peerID) throws IOException {
        this.peerID = peerID;
        this.logger = new Logging();

        // Setup config parsers
        commonConfig = new CommonConfig();
        peerInfoConfig = new PeerInfoConfig();
        metadata = peerInfoConfig.getPeerInfo(peerID);

        // Initialize socket and neighbor information storage
        socketConnectionsByNeighborID = new HashMap<>();
        bitfieldsByNeighborID = new HashMap<>();
        neighborsContacted = new HashSet<>();

        initializePieceIndexes();

        // Setup Listener port
        try {
            listenerSocket = new ServerSocket(metadata.getListeningPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializePieceIndexes() {
        numberOfPieces =
                (int)
                        Math.ceil(
                                (float) commonConfig.getFileSize()
                                        / (float) commonConfig.getPieceSize());
        pieceIndexes = new AtomicReferenceArray<>(numberOfPieces);

        for (int pieceId = 0; pieceId < numberOfPieces; pieceId++) {
            if (metadata.isHasFile()) {
                pieceIndexes.set(pieceId, true);
            } else {
                pieceIndexes.set(pieceId, false);
            }
        }
    }

    private void sendHandshake(final int neighborPeerID, final PeerMetadata metadata) {

        // Create HandshakeMessage object
        final HandshakeMessage handshakeMessage = new HandshakeMessage(peerID);
        try {

            // Get stored socked for neighbor and setup input and output streams
            final Socket neighborSocket = socketConnectionsByNeighborID.get(neighborPeerID);
            final ObjectOutputStream outputStream =
                    new ObjectOutputStream(neighborSocket.getOutputStream());
            outputStream.flush();
            ObjectInputStream inputStream = new ObjectInputStream(neighborSocket.getInputStream());

            // Sends handshake message
            outputStream.writeObject(handshakeMessage);
            System.out.println("Sent handshake message to: " + handshakeMessage);
            outputStream.flush();

            new PeerConnection(
                            peerID,
                            neighborSocket,
                            neighborsContacted,
                            pieceIndexes,
                            outputStream,
                            inputStream,
                    bitfieldsByNeighborID)
                    .start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Creates the socket connection with a specific neighbor and stores it
    private void setupConnection(int neighborPeerID, final PeerMetadata metadata) {
        try {

            final Socket neighborSocket =
                    new Socket(metadata.getHostName(), metadata.getListeningPort());
            socketConnectionsByNeighborID.put(neighborPeerID, neighborSocket);
            neighborsContacted.add(neighborPeerID);

            // Log connection
            logger.TCP_connect(this.peerID, neighborPeerID);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Constantly listens for connections
    private void listenForConnections() {
        try {
            while (true) {
                Socket socket = listenerSocket.accept();
                new PeerConnection(peerID, socket, neighborsContacted, pieceIndexes, bitfieldsByNeighborID).start();
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

    // Handles checking previously started neighbors and connects to them
    public void run() {
        peerInfoConfig
                .getPrevPeers(peerID)
                .forEach((neighborPeerID, metadata) -> setupConnection(neighborPeerID, metadata));

        peerInfoConfig
                .getPrevPeers(peerID)
                .forEach((neighborPeerID, metadata) -> sendHandshake(neighborPeerID, metadata));

        listenForConnections();
    }

    public static void main(String args[]) throws IOException {
        // Setup Peer
        Peer client = new Peer(Integer.parseInt(args[0]));
        System.out.println("Process " + "\u001B[31m" + args[0] + "\u001B[0m" + " running.");
        client.run();
    }
}
