package messages.Payload;

import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReferenceArray;

import static junit.framework.Assert.assertTrue;

public class BitfieldPayloadTest {
    @Test
    public void testBuildingPayloadForOneByte() {
        AtomicReferenceArray<Boolean> pieces = new AtomicReferenceArray<>(8);
        for (int i = 0; i < 8; i++) {
            pieces.set(i, true);
        }
        BitfieldPayload payloadMsg = new BitfieldPayload(pieces);
        byte[] sentPayload = payloadMsg.getPayload();

        boolean[] expected = convertAtomicReferenceArray(pieces);
        boolean[] actual = convertAtomicReferenceArray(new BitfieldPayload(8, sentPayload).getPieces());

        assertTrue(Arrays.equals(expected, actual));
    }

    @Test
    public void testBuildingPayloadForOneByteWithOneFalse() {
        AtomicReferenceArray<Boolean> pieces = new AtomicReferenceArray<>(8);
        for (int i = 0; i < 8; i++) {
            pieces.set(i, true);
        }
        pieces.set(2, false);
        BitfieldPayload payloadMsg = new BitfieldPayload(pieces);
        byte[] sentPayload = payloadMsg.getPayload();

        boolean[] expected = convertAtomicReferenceArray(pieces);
        boolean[] actual = convertAtomicReferenceArray(new BitfieldPayload(8, sentPayload).getPieces());

        assertTrue(Arrays.equals(expected, actual));
    }


    @Test
    public void testBuildingPayloadForOneByteWithMostlyFalse() {
        AtomicReferenceArray<Boolean> pieces = new AtomicReferenceArray<>(8);
        for (int i = 0; i < 8; i++) {
            pieces.set(i, false);
        }
        pieces.set(3, true);
        BitfieldPayload payloadMsg = new BitfieldPayload(pieces);
        byte[] sentPayload = payloadMsg.getPayload();

        boolean[] expected = convertAtomicReferenceArray(pieces);
        boolean[] actual = convertAtomicReferenceArray(new BitfieldPayload(8, sentPayload).getPieces());

        assertTrue(Arrays.equals(expected, actual));
    }

    @Test
    public void testBuildingPayloadForOneByteWithNotFullBytes() {
        AtomicReferenceArray<Boolean> pieces = new AtomicReferenceArray<>(9);
        for (int i = 0; i < 9; i++) {
            pieces.set(i, false);
        }
        pieces.set(3, true);
        BitfieldPayload payloadMsg = new BitfieldPayload(pieces);
        byte[] sentPayload = payloadMsg.getPayload();

        boolean[] expected = convertAtomicReferenceArray(pieces);
        boolean[] actual = convertAtomicReferenceArray(new BitfieldPayload(9, sentPayload).getPieces());

        System.out.println("exp: " + expected);
        System.out.println("act: " + actual);

        assertTrue(Arrays.equals(expected, actual));
    }


    public boolean[] convertAtomicReferenceArray(AtomicReferenceArray<Boolean> atomicReferenceArray) {
        boolean[] array = new boolean[atomicReferenceArray.length()];
        for (int i = 0; i < atomicReferenceArray.length(); i++) {
            array[i] = atomicReferenceArray.get(i);
        }
        return array;
    }

}
