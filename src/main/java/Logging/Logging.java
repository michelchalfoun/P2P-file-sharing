package Logging;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class Logging {

    //Setup to be used throughout logging functions
    Logger logger;
    FileHandler fh;
    SimpleFormatter formatter;
    private final int peerID;

    public Logging(final int peerID) throws IOException {
        this.peerID = peerID;
        this.logger = Logger.getLogger("MyLog");
        this.logger.setUseParentHandlers(false);
        this.fh = new FileHandler(Paths.get(System.getProperty("user.dir")).getParent().getParent() + "/logs/log_peer_" + peerID + ".log", true);
        logger.addHandler(this.fh);
        this.formatter = new SimpleFormatter();
        this.fh.setFormatter(this.formatter);
    }

    public void TCP_connect(final int peerID_2) {
        this.logger.info("Peer " + this.peerID + " makes a connection to Peer " + peerID_2 + ".");
    }

    public void changePreferredNeighbors(final int[] neighborID_list) {
        String msg = "Peer " + this.peerID + " has the preferred neighbors ";
        for (int id : neighborID_list) {
            msg += id + ", ";
        }
        //Remove last trailing comma and space
        this.logger.info(msg.substring(0, msg.length() - 2) + ".");
    }

    public void changeOptimUnchokedNeighbor(final int neighborID) {
        this.logger.info("Peer " + this.peerID + " has the optimistically unchoked neighbor " + neighborID + ".");
    }

    public void unchoking(final int peerID_2) {
        this.logger.info("Peer " + this.peerID + " is unchoked by " + peerID_2 + ".");
    }

    public void choking(final int peerID_2) {
        this.logger.info("Peer " + this.peerID + " is choked by " + peerID_2 + ".");
    }

    public void receiveHaveMsg(final int peerID_2, final int pieceIndex) {
        this.logger.info("Peer " + this.peerID + " received the 'have' message from " + peerID_2 + " for the piece " + pieceIndex + ".");
    }

    public void receiveInterestedMsg(final int peerID_2) {
        this.logger.info("Peer " + this.peerID + " received the 'interested' message from " + peerID_2 + ".");
    }

    public void receiveNotInterestedMsg(final int peerID_2) {
        this.logger.info("Peer " + this.peerID + " received the 'not interested' message from " + peerID_2 + ".");
    }

    public void downloadingPiece(final int peerID_2, final int pieceIndex, final int numOfPieces) {
        this.logger.info("Peer " + this.peerID + " has downloadded the piece " + pieceIndex + " from " + peerID_2 + ". Now the number of pieces it has is " + numOfPieces + ".");
    }

    public void completionOfDownload() {
        this.logger.info("Peer " + this.peerID + " has downloaded the complete file");
    }
}
