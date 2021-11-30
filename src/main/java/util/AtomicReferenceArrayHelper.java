package util;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class AtomicReferenceArrayHelper
{
    public static boolean isInterested(final AtomicReferenceArray<Boolean> peer, final AtomicReferenceArray<Boolean> neighbor) {
        boolean isInterested = false;

        System.out.println("peer: " + peer.length());
        System.out.println("neighbor: " + neighbor.length());

        for (int index = 0; index < neighbor.length(); index++) {
            if (neighbor.get(index) && !peer.get(index)) {
                isInterested = true;
            }
        }

        return isInterested;
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
