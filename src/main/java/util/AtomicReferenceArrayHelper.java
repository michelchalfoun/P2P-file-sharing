//package util;
//
//import pieces.Pieces;
//
//import java.util.concurrent.atomic.AtomicReferenceArray;
//
//public class AtomicReferenceArrayHelper
//{
//    public static boolean isInterested(final Pieces peer, final Pieces neighbor) {
//        boolean isInterested = false;
//
//        for (int index = 0; index < neighbor.length(); index++) {
//            if (neighbor.get(index) && !peer.get(index)) {
//                isInterested = true;
//            }
//        }
//
//        return isInterested;
//    }
//
//    // Convert AtomicReferenceArray<Boolean> to boolean[]
//    private static boolean[] convertAtomicReferenceArray(
//            final AtomicReferenceArray<Boolean> atomicReferenceArray) {
//        boolean[] array = new boolean[atomicReferenceArray.length()];
//        for (int i = 0; i < atomicReferenceArray.length(); i++) {
//            array[i] = atomicReferenceArray.get(i);
//        }
//        return array;
//    }
//}
