package pieces;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PieceManager {
    //    public static void main(String[] args) {
    //
    //        PieceManager m = new PieceManager(1001, "lol2", 102, 5, true);
    //
    //        int s = 0;
    //        byte[] total = new byte[102];
    //
    //        PieceManager m2 = new PieceManager(1001, "lol2", 102, 5, true);
    //
    //
    //        for (int i = 0; i < 21; i++) {
    //            byte[] test = m.getPiece(i);
    //            for (int j = 0; j < test.length; j++) total[s++] = test[j];
    //            m2.savePiece(i, test);
    //        }
    //
    //        System.out.println(new String(total, StandardCharsets.UTF_8));
    //
    //        try {
    //            m2.consolidatePiecesIntoFile();
    //        } catch (IOException e) {
    //            e.printStackTrace();
    //        }
    //
    //    }

    private final int peerID;
    private final String fileName;
    private final int pieceSize;
    private final int numberOfPieces;
    private final int fileSize;
    private boolean hasFile;

    public PieceManager(
            final int peerID,
            final String fileName,
            final int fileSize,
            final int pieceSize,
            final boolean hasFile) {
        this.peerID = peerID;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.pieceSize = pieceSize;
        this.hasFile = hasFile;
        this.numberOfPieces = (int) Math.ceil((float) fileSize / (float) pieceSize);
    }

    public byte[] getPiece(final int pieceID) {
        if (hasFile) {
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
        hasFile = true;
    }

    private void createFileWithBytes(final String fileName, final byte[] bytes) {
        try {
            final File file = new File(fileName);
            file.createNewFile();
            Files.write(file.toPath(), bytes);
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
