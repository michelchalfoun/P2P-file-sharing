package peer;

import config.CommonConfig;
import config.PeerInfoConfig;
import neighbor.Neighbor;
import pieces.PieceManager;
import timer.UnchokingTimer;
import messages.HandshakeMessage;
import logging.Logging;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * Main class that sets up the main Peer process (spins up all of the listener threads and keeps
 * track of data)
 */
public class Peer {
    private final Logging logger;

    private final int peerID;

    private final CommonConfig commonConfig;
    private final PeerInfoConfig peerInfoConfig;

    private UnchokingTimer unchokingTimer;

    private final Map<Integer, Neighbor> neighborData;
    private AtomicReferenceArray<Boolean> pieceIndexes;
    private final PeerMetadata metadata;

    private ServerSocket listenerSocket;

    private Set<Integer> requestedPieces;

    public Peer(final int peerID) throws IOException {

//        neighborData.entrySet().stream().filter(entry -> entry.getValue().getChoke()).collect(Collectors.toList());
        this.peerID = peerID;

        logger = Logging.getInstance();
        logger.setPeerID(peerID);

        // Setup config parsers
        commonConfig = new CommonConfig();
        peerInfoConfig = new PeerInfoConfig();
        metadata = peerInfoConfig.getPeerInfo(peerID);

        // Initialize socket and neighbor information storage
        neighborData = new ConcurrentHashMap<>();

        initializePieceIndexes();

        ConcurrentHashMap<Integer, Integer> map = new ConcurrentHashMap<>();
        requestedPieces = map.newKeySet();

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
        final int numberOfPieces = (int) Math.ceil(fileSize / pieceSize);
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
            final Socket neighborSocket = neighborData.get(neighborPeerID).getSocket();
            final ObjectOutputStream outputStream =
                    new ObjectOutputStream(neighborSocket.getOutputStream());
            outputStream.flush();
            ObjectInputStream inputStream = new ObjectInputStream(neighborSocket.getInputStream());

            // Sends handshake message
            outputStream.writeObject(handshakeMessage);
            outputStream.flush();

            final PieceManager pieceManager = new PieceManager(peerID, commonConfig.getFileName(), commonConfig.getFileSize(), commonConfig.getPieceSize(), metadata.isHasFile());
            new PeerConnection(
                            peerID,
                            neighborSocket,
                            true,
                            pieceIndexes,
                            neighborData,
                            outputStream,
                            inputStream,
                            pieceManager,
                            requestedPieces)
                    .start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // TODO: move this to sendHandshake
    // Creates the socket connection with a specific neighbor and stores it
    private void setupConnection(int neighborPeerID, final PeerMetadata metadata) {
        try {

            final Socket neighborSocket =
                    new Socket(metadata.getHostName(), metadata.getListeningPort());
            neighborData.put(neighborPeerID, new Neighbor(neighborSocket, neighborPeerID));

            // Log connection
            logger.TCP_connect(peerID, neighborPeerID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Constantly listens for connections
    private void listenForConnections() {
        try {
            while (true) {
                final Socket socket = listenerSocket.accept();
                final PieceManager pieceManager = new PieceManager(peerID, commonConfig.getFileName(), commonConfig.getFileSize(), commonConfig.getPieceSize(), metadata.isHasFile());
                new PeerConnection(peerID, socket, pieceIndexes, neighborData, pieceManager, requestedPieces).start();
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

        unchokingTimer = new UnchokingTimer(peerID, commonConfig.getUnchokingInterval(), commonConfig.getOptimisticUnchokingInterval(), commonConfig.getNumberOfPreferredNeighbors(), neighborData);
        unchokingTimer.start();

//        optimisticUnchokingTimer = new OptimisticUnchokingTimer(commonConfig.getOptimisticUnchokingInterval());
//        optimisticUnchokingTimer.start();

        listenForConnections();
    }

    public static void main(String args[]) throws IOException {
        final int peerID = Integer.parseInt(args[0]);
        final Peer client = new Peer(peerID);
        System.out.println("Process " + "\u001B[31m" + peerID + "\u001B[0m" + " running.");
        client.run();
    }
}
