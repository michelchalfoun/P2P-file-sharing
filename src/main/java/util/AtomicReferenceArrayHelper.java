package util;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class AtomicReferenceArrayHelper
{
    public static boolean isEqual(final AtomicReferenceArray<Boolean> piecesOne, final AtomicReferenceArray<Boolean> piecesTwo) {
        boolean[] convertedPieces = convertAtomicReferenceArray(piecesOne);
        boolean[] currentPieces = convertAtomicReferenceArray(piecesTwo);
        return Arrays.equals(convertedPieces, currentPieces);
    }

    // Convert AtomicReferenceArray<Boolean> to boolean[]
    private static boolean[] convertAtomicReferenceArray(
            final AtomicReferenceArray<Boolean> atomicReferenceArray) {
        boolean[] array = new boolean[atomicReferenceArray.length()];
        for (int i = 0; i < atomicReferenceArray.length(); i++) {
            array[i] = atomicReferenceArray.get(i);
        }
        return array;
    }
}
