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

        /**
         * When a connection is unchoked by a neighbor, a peer sends a ‘request’ message for
         * requesting a piece that it does not have and has not requested from other neighbors.
         *
         * <p>Even though peer A sends a ‘request’ message to peer B, it may not receive a ‘piece’
         * message corresponding to it. This situation happens when peer B re-determines preferred
         * neighbors or optimistically unchoked a neighbor and peer A is choked as the result before
         * peer B responds to peer A. Your program should consider this case
         */
        for (int index = 0; index < neighborBitfield.length(); index++) {
            if (neighborBitfield.get(index) && !peerBitfield.get(index)) {
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
