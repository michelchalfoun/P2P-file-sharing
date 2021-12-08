package logging;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

/** Logger class to log peer actions */
public class Logging {
    private static Logging logging;

    private int peerID;

    private final Logger logger;
    private FileHandler fileHandler;
    private CustomFormatter formatter;

    public Logging() {
        logger = Logger.getLogger("P2PLog");
        logger.setUseParentHandlers(false);
    }

    private void validateLogFolder() {
        final File directoryFolder = new File(Paths.get(System.getProperty("user.dir")) + "/logs");
        if (!directoryFolder.exists()) {
            directoryFolder.mkdir();
        }
    }

    private void setFile() throws IOException {
        validateLogFolder();

        // Might need to change this to protect from possible path issues
        this.fileHandler =
                new FileHandler(
                        Paths.get(System.getProperty("user.dir"))
                                + "/logs/log_peer_"
                                + this.peerID
                                + ".log",
                        true);
        logger.addHandler(this.fileHandler);
        this.formatter = new CustomFormatter();
        this.fileHandler.setFormatter(new CustomFormatter());
    }

    public void TCP_connect(final int peerID_1, final int peerID_2) {
        logInfo("Peer " + peerID_1 + " makes a connection to Peer " + peerID_2 + ".");
    }

    public void TCP_receive(final int peerID_1, final int peerID_2) {
        logInfo("Peer " + peerID_1 + " is connected from Peer " + peerID_2 + ".");
    }

    public void changePreferredNeighbors(final int peerID_1, final Set<Integer> neighborID_list) {
        String msg = "Peer " + peerID_1 + " has the preferred neighbors ";
        for (int id : neighborID_list) {
            msg += id + ", ";
        }
        // Remove last trailing comma and space
        logInfo(msg.substring(0, msg.length() - 2) + ".");
    }

    public void changeOptimUnchokedNeighbor(final int peerID_1, final int neighborID) {
        logInfo(
                "Peer "
                        + peerID_1
                        + " has the optimistically unchoked neighbor "
                        + neighborID
                        + ".");
    }

    public void unchoking(final int peerID_1, final int peerID_2) {
        logInfo("Peer " + peerID_1 + " is unchoked by " + peerID_2 + ".");
    }

    public void choking(final int peerID_1, final int peerID_2) {
        logInfo("Peer " + peerID_1 + " is choked by " + peerID_2 + ".");
    }

    public void receiveHaveMsg(final int peerID_1, final int peerID_2, final int pieceIndex) {
        logInfo(
                "Peer "
                        + peerID_1
                        + " received the 'have' message from "
                        + peerID_2
                        + " for the piece "
                        + pieceIndex
                        + ".");
    }

    public void receiveInterestedMsg(final int peerID_1, final int peerID_2) {
        logInfo(
                "Peer " + peerID_1 + " received the 'interested' message from " + peerID_2 + ".");
    }

    public void receiveNotInterestedMsg(final int peerID_1, final int peerID_2) {
        logInfo(
                "Peer "
                        + peerID_1
                        + " received the 'not interested' message from "
                        + peerID_2
                        + ".");
    }

    public void downloadingPiece(
            final int peerID_1, final int peerID_2, final int pieceIndex, final int numOfPieces) {
        logInfo(
                "Peer "
                        + peerID_1
                        + " has downloaded the piece "
                        + pieceIndex
                        + " from "
                        + peerID_2
                        + ". Now the number of pieces it has is "
                        + numOfPieces
                        + ".");
    }

    public void completionOfDownload(final int peerID_1) {
        logInfo("Peer " + peerID_1 + " has downloaded the complete file");
    }

    public void custom(final String string) {
        logInfo(string);
    }

    public void customErr(final Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String sStackTrace = sw.toString(); // stack trace as a string
        custom(sStackTrace);
    }

    private void logInfo(final String message) {
        logger.info(message);
    }

    public static Logging getInstance() {
        if (logging == null) {
            logging = new Logging();
        }
        return logging;
    }

    public void setPeerID(final int peerID) {
        this.peerID = peerID;
        try {
            setFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
