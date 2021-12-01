//package messages.payload;
//
//import messages.payload.impl.BitfieldPayload;
//import org.junit.Test;
//
//import java.util.Arrays;
//import java.util.concurrent.atomic.AtomicReferenceArray;
//
//import static junit.framework.Assert.assertTrue;
//
//public class BitfieldPayloadTest {
//    @Test
//    public void testBuildingPayloadForOneByte() {
//        AtomicReferenceArray<Boolean> pieces = new AtomicReferenceArray<>(8);
//        for (int i = 0; i < 8; i++) {
//            pieces.set(i, true);
//        }
//        BitfieldPayload payloadMsg = new BitfieldPayload(pieces);
//        byte[] sentPayload = payloadMsg.getBytes();
//
//        boolean[] expected = convertAtomicReferenceArray(pieces);
//        boolean[] actual = convertAtomicReferenceArray(new BitfieldPayload(8, sentPayload).getPieces());
//
//        assertTrue(Arrays.equals(expected, actual));
//    }
//
//    @Test
//    public void testBuildingPayloadForOneByteWithOneFalse() {
//        AtomicReferenceArray<Boolean> pieces = new AtomicReferenceArray<>(8);
//        for (int i = 0; i < 8; i++) {
//            pieces.set(i, true);
//        }
//        pieces.set(2, false);
//        BitfieldPayload payloadMsg = new BitfieldPayload(pieces);
//        byte[] sentPayload = payloadMsg.getBytes();
//
//        boolean[] expected = convertAtomicReferenceArray(pieces);
//        boolean[] actual = convertAtomicReferenceArray(new BitfieldPayload(8, sentPayload).getPieces());
//
//        assertTrue(Arrays.equals(expected, actual));
//    }
//
//
//    @Test
//    public void testBuildingPayloadForOneByteWithMostlyFalse() {
//        AtomicReferenceArray<Boolean> pieces = new AtomicReferenceArray<>(8);
//        for (int i = 0; i < 8; i++) {
//            pieces.set(i, false);
//        }
//        pieces.set(3, true);
//        BitfieldPayload payloadMsg = new BitfieldPayload(pieces);
//        byte[] sentPayload = payloadMsg.getBytes();
//
//        boolean[] expected = convertAtomicReferenceArray(pieces);
//        boolean[] actual = convertAtomicReferenceArray(new BitfieldPayload(8, sentPayload).getPieces());
//
//        assertTrue(Arrays.equals(expected, actual));
//    }
//
//    @Test
//    public void testBuildingPayloadForOneByteWithNotFullBytes() {
//        AtomicReferenceArray<Boolean> pieces = new AtomicReferenceArray<>(9);
//        for (int i = 0; i < 9; i++) {
//            pieces.set(i, false);
//        }
//        pieces.set(3, true);
//        BitfieldPayload payloadMsg = new BitfieldPayload(pieces);
//        byte[] sentPayload = payloadMsg.getBytes();
//
//        boolean[] expected = convertAtomicReferenceArray(pieces);
//        boolean[] actual = convertAtomicReferenceArray(new BitfieldPayload(9, sentPayload).getPieces());
//
//        assertTrue(Arrays.equals(expected, actual));
//    }
//
//    @Test
//    public void testBuildingPayloadForOneByteWithThreeBytes() {
//        AtomicReferenceArray<Boolean> pieces = new AtomicReferenceArray<>(20);
//        for (int i = 0; i < 20; i++) {
//            pieces.set(i, false);
//        }
//        pieces.set(3, true);
//        pieces.set(10, true);
//        pieces.set(15, true);
//        pieces.set(18, true);
//        BitfieldPayload payloadMsg = new BitfieldPayload(pieces);
//        byte[] sentPayload = payloadMsg.getBytes();
//
//        boolean[] expected = convertAtomicReferenceArray(pieces);
//        boolean[] actual = convertAtomicReferenceArray(new BitfieldPayload(20, sentPayload).getPieces());
//
//        assertTrue(Arrays.equals(expected, actual));
//    }
//
//
//    public boolean[] convertAtomicReferenceArray(AtomicReferenceArray<Boolean> atomicReferenceArray) {
//        boolean[] array = new boolean[atomicReferenceArray.length()];
//        for (int i = 0; i < atomicReferenceArray.length(); i++) {
//            array[i] = atomicReferenceArray.get(i);
//        }
//        return array;
//    }
//
//}
