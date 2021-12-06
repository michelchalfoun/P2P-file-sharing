package pieces;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class PieceManager {

    private final int peerID;
    private final String fileName;
    private final int pieceSize;
    private final int numberOfPieces;
    private final int fileSize;

    private final AtomicInteger numberOfPiecesDownloaded;

    public PieceManager(
            final int peerID,
            final String fileName,
            final int fileSize,
            final int pieceSize,
            final AtomicInteger numberOfPiecesDownloaded) {
        this.peerID = peerID;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.pieceSize = pieceSize;
        this.numberOfPieces = (int) Math.ceil((float) fileSize / (float) pieceSize);
        this.numberOfPiecesDownloaded = numberOfPiecesDownloaded;
    }

    public byte[] getPiece(final int pieceID) {
        if (numberOfPiecesDownloaded.get() == numberOfPieces) {
            try {
                byte[] allBytesInFile = Files.readAllBytes(Paths.get(getFilePath()));
                byte[] pieceBytes =
                        new byte[pieceID == numberOfPieces - 1 ? fileSize % pieceSize : pieceSize];

                final int startingByte = pieceID * pieceSize;
                final int endingByte = Math.min(fileSize, startingByte + pieceSize);
                for (int index = startingByte; index < endingByte; index++) {
                    pieceBytes[index - startingByte] = allBytesInFile[index];
                }
                return pieceBytes;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                return Files.readAllBytes(Paths.get(getPiecePath(pieceID)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void savePiece(final int pieceID, final byte[] pieceBytes) {
        createFileWithBytes(getPiecePath(pieceID), pieceBytes);
    }

    public void consolidatePiecesIntoFile() throws IOException {
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
