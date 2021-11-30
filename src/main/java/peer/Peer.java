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
 * Main class that sets up the main Peer process (spins up all of the listener threads and keeps
 * track of data)
 */
public class Peer {
    private final int peerID;

    private final CommonConfig commonConfig;
    private final PeerInfoConfig peerInfoConfig;

    private final PeerMetadata metadata;

    private final Map<Integer, Socket> socketConnectionsByNeighborID;
    private final Map<Integer, AtomicReferenceArray> bitfieldsByNeighborID;

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

        initializePieceIndexes();

        // Setup Listener port
        try {
            listenerSocket = new ServerSocket(metadata.getListeningPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializePieceIndexes() {
        final float fileSize = commonConfig.getFileSize();
        final float pieceSize = commonConfig.getPieceSize();
        numberOfPieces = (int) Math.ceil(fileSize / pieceSize);
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
                            true,
                            pieceIndexes,
                            bitfieldsByNeighborID,
                            outputStream,
                            inputStream)
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
                new PeerConnection(peerID, socket, pieceIndexes, bitfieldsByNeighborID).start();
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
        peerInfoConfig.getPrevPeers(peerID).forEach(this::setupConnection);

        peerInfoConfig.getPrevPeers(peerID).forEach(this::sendHandshake);

        listenForConnections();
    }

    public static void main(String args[]) throws IOException {
        // Setup Peer
        final int peerID = Integer.parseInt(args[0]);
        //        final int peerID = 1001;
        final Peer client = new Peer(peerID);
        System.out.println("Process " + "\u001B[31m" + peerID + "\u001B[0m" + " running.");
        client.run();
    }
}
