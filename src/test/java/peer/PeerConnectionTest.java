package peer;

import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReferenceArray;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class PeerConnectionTest {

    @Test
    public void testComparePiecesForEqualArray() {
        AtomicReferenceArray<Boolean> curPieces = new AtomicReferenceArray<>(8);
        for (int i = 0; i < 8; i++) {
            curPieces.set(i, true);
        }

        AtomicReferenceArray<Boolean> recPieces = new AtomicReferenceArray<>(8);
        for (int i = 0; i < 8; i++) {
            recPieces.set(i, true);
        }

        PeerConnection testConnection = new PeerConnection(1, null);

        assertTrue(testConnection.comparePieces(curPieces, recPieces));
    }

    @Test
    public void testComparePiecesForUnequalArray() {
        AtomicReferenceArray<Boolean> curPieces = new AtomicReferenceArray<>(8);
        for (int i = 0; i < 8; i++) {
            curPieces.set(i, false);
        }

        AtomicReferenceArray<Boolean> recPieces = new AtomicReferenceArray<>(8);
        for (int i = 0; i < 8; i++) {
            recPieces.set(i, true);
        }

        PeerConnection testConnection = new PeerConnection(1, null);

        assertFalse(testConnection.comparePieces(curPieces, recPieces));
    }
}
