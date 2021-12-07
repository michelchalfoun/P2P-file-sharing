package pieces;

import logging.Logging;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PieceManager {

    private final int peerID;
    private final String fileName;
    private final int pieceSize;
    private final int numberOfPieces;
    private final int fileSize;
    private final Pieces pieces;
//    private final ReentrantReadWriteLock consolidationLock;

    private final AtomicBoolean isConsolidated;

    public PieceManager(
            final int peerID,
            final String fileName,
            final int fileSize,
            final int pieceSize,
            final Pieces pieces) {
        this.peerID = peerID;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.pieceSize = pieceSize;
        this.pieces = pieces;
        numberOfPieces = (int) Math.ceil((float) fileSize / (float) pieceSize);
//        consolidationLock = new ReentrantReadWriteLock();
        isConsolidated = new AtomicBoolean(false);
    }

    public void convertToPieces() {
        try {
            byte[] allBytesInFile = Files.readAllBytes(Paths.get(getFilePath()));
            for (int pieceID = 0; pieceID < pieces.getNumberOfPieces(); pieceID++) {
                byte[] pieceBytes =
                        new byte[pieceID == numberOfPieces - 1 ? fileSize % pieceSize : pieceSize];

                final int startingByte = pieceID * pieceSize;
                final int endingByte = Math.min(fileSize, startingByte + pieceSize);
                for (int index = startingByte; index < endingByte; index++) {
                    pieceBytes[index - startingByte] = allBytesInFile[index];
                }
                savePiece(pieceID, pieceBytes);
            }
            new File(getFilePath()).delete();
        } catch (IOException e) {
            logErr(e);
            e.printStackTrace();
        }
    }

    // TODO: remove param
    public byte[] getPiece(final int pieceID) {
//        consolidationLock.readLock().lock();
//        if (pieces.getNumberOfPiecesDownloaded() == numberOfPieces) {
//            // Has consolidated file
//            try {
//                byte[] allBytesInFile = Files.readAllBytes(Paths.get(getFilePath()));
//                byte[] pieceBytes =
//                        new byte[pieceID == numberOfPieces - 1 ? fileSize % pieceSize : pieceSize];
//
//                final int startingByte = pieceID * pieceSize;
//                final int endingByte = Math.min(fileSize, startingByte + pieceSize);
//                for (int index = startingByte; index < endingByte; index++) {
//                    pieceBytes[index - startingByte] = allBytesInFile[index];
//                }
////                consolidationLock.readLock().unlock();
//                return pieceBytes;
//            } catch (IOException e) {
//                logErr(e);
//                e.printStackTrace();
//            }
//        } else {
            // Has remaining pieces (sec.jpg-piece-2)
            try {
                byte[] pieceBytes = Files.readAllBytes(Paths.get(getPiecePath(pieceID)));
//                consolidationLock.readLock().unlock();
                return pieceBytes;
            } catch (IOException e) {
                logErr(e);
                e.printStackTrace();
            }
//        }
        return null;
    }

    public void savePiece(final int pieceID, final byte[] pieceBytes) {
        createFileWithBytes(getPiecePath(pieceID), pieceBytes);
    }

    // TODO: remove param
    public void consolidatePiecesIntoFile() {
        //        consolidationLock.writeLock().lock();
        if (isConsolidated.compareAndSet(false, true)) {
            Logging.getInstance().custom("Initializing consolidation!");
            try {
                final byte[] fileBytes = new byte[fileSize];
                int currentByte = 0;
                for (int pieceIndex = 0; pieceIndex < numberOfPieces; pieceIndex++) {
                    final File newPiece = new File(getPiecePath(pieceIndex));
                    final byte[] pieceBytes = Files.readAllBytes(newPiece.toPath());
                    for (byte pieceByte : pieceBytes) {
                        fileBytes[currentByte++] = pieceByte;
                    }
                    newPiece.delete();
                }
                createFileWithBytes(getFilePath(), fileBytes);
            } catch (IOException e) {
                logErr(e);
                e.printStackTrace();
            }
            Logging.getInstance().custom("Completed consolidation!");
            //        consolidationLock.writeLock().unlock();
        }
    }

    private void logErr(IOException e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();
        Logging.getInstance().custom(pieces.toString());
        Logging.getInstance().custom(exceptionAsString);
//        Logging.getInstance().custom("NUMBER OF PIECES: " + numberOfPiecesDownloaded.get() + "/" + numberOfPieces);
    }
    public boolean hasAllPieces() {
        return pieces.getNumberOfPiecesDownloaded() == numberOfPieces;
    }

    private void createFileWithBytes(final String fileName, final byte[] bytes) {
        try {
            final File file = new File(fileName);
            final File parent = file.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                throw new IllegalStateException("Couldn't create dir: " + parent);
            }
            file.createNewFile();
            Files.write(file.toPath(), bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE, StandardOpenOption.SYNC);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getPiecePath(final int pieceID) {
        return getFilePath() + "_piece_" + pieceID;
    }

    private String getFilePath() {
        return System.getProperty("user.dir") + "/" + peerID + "/" + fileName;
    }
}
