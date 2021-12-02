package util;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.ThreadLocalRandom;

public class RandomMissingPieceGenerator {

    AtomicReferenceArray<Boolean> curBitfield;
    AtomicReferenceArray<Boolean> neighborBitfield;

    RandomMissingPieceGenerator(AtomicReferenceArray<Boolean> curBitfield,
                                AtomicReferenceArray<Boolean> neighborBitfield){
        this.curBitfield = curBitfield;
        this.neighborBitfield = neighborBitfield;
    }

    public int getRandomPiece(){
        ArrayList<Integer> missingPieces = new ArrayList<>();

        for (int i = 0; i < neighborBitfield.length(); i++){
            if (neighborBitfield.get(i) && !curBitfield.get(i)){
                missingPieces.add(i);
            }
        }

        if (missingPieces.size() == 0) {
            return -1;
        } else {
            int randomNum = ThreadLocalRandom.current().nextInt(0, missingPieces.size());
            return missingPieces.get(randomNum);
        }

    }
}
