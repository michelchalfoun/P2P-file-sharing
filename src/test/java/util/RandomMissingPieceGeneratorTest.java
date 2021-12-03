package util;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReferenceArray;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class RandomMissingPieceGeneratorTest {

    @Test
    public void testRandomMissingPieceGenerator() {
        final AtomicReferenceArray<Boolean> peerBitfield = generateEmptyBitfield(10);
        final AtomicReferenceArray<Boolean> neighborBitfield = generateEmptyBitfield(10);

        final Set<Integer> piecesHeldInNeighbor = new HashSet<>(Arrays.asList(4, 6, 8));
        piecesHeldInNeighbor.forEach(pieceID -> neighborBitfield.set(pieceID, true));

        final RandomMissingPieceGenerator generator =
                new RandomMissingPieceGenerator(peerBitfield, neighborBitfield);

        for (int iteration = 0; iteration < 100; iteration++) {
            assertTrue(piecesHeldInNeighbor.contains(generator.getRandomPiece()));
        }
    }

    @Test
    public void testRandomMissingPieceGeneratorWithNoMissingPieces() {
        final AtomicReferenceArray<Boolean> peerBitfield = generateFullBitfield(10);
        final AtomicReferenceArray<Boolean> neighborBitfield = generateEmptyBitfield(10);

        final Set<Integer> piecesHeldInNeighbor = new HashSet<>(Arrays.asList(4, 6, 8));
        piecesHeldInNeighbor.forEach(pieceID -> neighborBitfield.set(pieceID, true));

        final RandomMissingPieceGenerator generator =
                new RandomMissingPieceGenerator(peerBitfield, neighborBitfield);

        for (int iteration = 0; iteration < 100; iteration++) {
            assertEquals(-1, generator.getRandomPiece());
        }
    }

    // TODO: abstract this out (repeated code)
    private AtomicReferenceArray<Boolean> generateEmptyBitfield(final int size) {
        return generateBitfield(size, false);
    }

    private AtomicReferenceArray<Boolean> generateFullBitfield(final int size) {
        return generateBitfield(size, true);
    }

    private AtomicReferenceArray<Boolean> generateBitfield(final int size, final boolean value) {
        final AtomicReferenceArray<Boolean> pieces = new AtomicReferenceArray<>(size);
        for (int i = 0; i < size; i++) {
            pieces.set(i, value);
        }
        return pieces;
    }
}
