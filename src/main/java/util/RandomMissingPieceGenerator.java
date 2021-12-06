package util;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.ThreadLocalRandom;

public class RandomMissingPieceGenerator {

    final AtomicReferenceArray<Boolean> peerBitfield;
    final AtomicReferenceArray<Boolean> neighborBitfield;
    final ThreadLocalRandom threadLocalRandom;
    final Set<Integer> requestedPieces;

    public RandomMissingPieceGenerator(
            final AtomicReferenceArray<Boolean> peerBitfield,
            final AtomicReferenceArray<Boolean> neighborBitfield,
            final Set<Integer> requestedPieces) {
        this.peerBitfield = peerBitfield;
        this.neighborBitfield = neighborBitfield;
        this.requestedPieces = requestedPieces;
        threadLocalRandom = ThreadLocalRandom.current();
    }

    // TODO: synchronize
    public synchronized int getRandomPiece() {
        final ArrayList<Integer> missingPieces = new ArrayList<>();

        for (int index = 0; index < neighborBitfield.length(); index++) {
            if (neighborBitfield.get(index) && !peerBitfield.get(index) && !requestedPieces.contains(index)) {
                missingPieces.add(index);
            }
        }

        if (missingPieces.size() == 0) {
            return -1;
        } else {
            int randomNum = threadLocalRandom.nextInt(0, missingPieces.size());
            return missingPieces.get(randomNum);
        }
    }
}
