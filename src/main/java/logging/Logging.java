package logging;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/** Logger class to log peer actions */
public class Logging
{
    // Setup to be used throughout logging functions
    Logger logger;
    FileHandler fh;
    SimpleFormatter formatter;

    public Logging() throws IOException {
        this.logger = Logger.getLogger("MyLog");
        this.logger.setUseParentHandlers(false);
    }

    private void setFile(final int peerID_1) throws IOException {
        // Might need to change this to protect from possible path issues
        this.fh =
                new FileHandler(
                        Paths.get(System.getProperty("user.dir"))
                                + "/logs/log_peer_"
                                + peerID_1
                                + ".log",
                        true);
        logger.addHandler(this.fh);
        this.formatter = new SimpleFormatter();
        this.fh.setFormatter(this.formatter);
    }

    public void TCP_connect(final int peerID_1, final int peerID_2) {
        try {
            setFile(peerID_1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.logger.info("Peer " + peerID_1 + " makes a connection to Peer " + peerID_2 + ".");
    }

    public void TCP_receive(final int peerID_1, final int peerID_2) {
        try {
            setFile(peerID_1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.logger.info("Peer " + peerID_1 + " is connected from Peer " + peerID_2 + ".");
    }

    public void changePreferredNeighbors(final int peerID_1, final int[] neighborID_list) {
        try {
            setFile(peerID_1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String msg = "Peer " + peerID_1 + " has the preferred neighbors ";
        for (int id : neighborID_list) {
            msg += id + ", ";
        }
        // Remove last trailing comma and space
        this.logger.info(msg.substring(0, msg.length() - 2) + ".");
    }

    public void changeOptimUnchokedNeighbor(final int peerID_1, final int neighborID) {
        try {
            setFile(peerID_1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.logger.info(
                "Peer "
                        + peerID_1
                        + " has the optimistically unchoked neighbor "
                        + neighborID
                        + ".");
    }

    public void unchoking(final int peerID_1, final int peerID_2) {
        try {
            setFile(peerID_1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.logger.info("Peer " + peerID_1 + " is unchoked by " + peerID_2 + ".");
    }

    public void choking(final int peerID_1, final int peerID_2) {
        try {
            setFile(peerID_1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.logger.info("Peer " + peerID_1 + " is choked by " + peerID_2 + ".");
    }

    public void receiveHaveMsg(final int peerID_1, final int peerID_2, final int pieceIndex) {
        try {
            setFile(peerID_1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.logger.info(
                "Peer "
                        + peerID_1
                        + " received the 'have' message from "
                        + peerID_2
                        + " for the piece "
                        + pieceIndex
                        + ".");
    }

    public void receiveInterestedMsg(final int peerID_1, final int peerID_2) {
        try {
            setFile(peerID_1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.logger.info(
                "Peer " + peerID_1 + " received the 'interested' message from " + peerID_2 + ".");
    }

    public void receiveNotInterestedMsg(final int peerID_1, final int peerID_2) {
        try {
            setFile(peerID_1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.logger.info(
                "Peer "
                        + peerID_1
                        + " received the 'not interested' message from "
                        + peerID_2
                        + ".");
    }

    public void downloadingPiece(
            final int peerID_1, final int peerID_2, final int pieceIndex, final int numOfPieces) {
        try {
            setFile(peerID_1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.logger.info(
                "Peer "
                        + peerID_1
                        + " has downloadded the piece "
                        + pieceIndex
                        + " from "
                        + peerID_2
                        + ". Now the number of pieces it has is "
                        + numOfPieces
                        + ".");
    }

    public void completionOfDownload(final int peerID_1) {
        try {
            setFile(peerID_1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.logger.info("Peer " + peerID_1 + " has downloaded the complete file");
    }
}
