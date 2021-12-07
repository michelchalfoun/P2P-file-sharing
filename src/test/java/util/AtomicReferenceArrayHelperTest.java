//package util;
//
//import org.junit.Test;
//
//import java.util.concurrent.atomic.AtomicReferenceArray;
//
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertTrue;
//
//public class AtomicReferenceArrayHelperTest {
//    @Test
//    public void testIsInterestedWhenInterested() {
//        final AtomicReferenceArray<Boolean> peerBitfield = generateEmptyBitfield(10);
//        final AtomicReferenceArray<Boolean> neighborBitfield = generateEmptyBitfield(10);
//
//        neighborBitfield.set(3, true);
//
//        assertTrue(AtomicReferenceArrayHelper.isInterested(peerBitfield, neighborBitfield));
//    }
//
//    @Test
//    public void testIsInterestedWhenNotInterested() {
//        final AtomicReferenceArray<Boolean> peerBitfield = generateEmptyBitfield(10);
//        final AtomicReferenceArray<Boolean> neighborBitfield = generateEmptyBitfield(10);
//
//        peerBitfield.set(3, true);
//
//        assertFalse(AtomicReferenceArrayHelper.isInterested(peerBitfield, neighborBitfield));
//    }
//
//    @Test
//    public void testIsInterestedWhenBothEmpty() {
//        final AtomicReferenceArray<Boolean> peerBitfield = generateEmptyBitfield(10);
//        final AtomicReferenceArray<Boolean> neighborBitfield = generateEmptyBitfield(10);
//
//        assertFalse(AtomicReferenceArrayHelper.isInterested(peerBitfield, neighborBitfield));
//    }
//
//    @Test
//    public void testIsInterestedWhenBothFull() {
//        final AtomicReferenceArray<Boolean> peerBitfield = generateFullBitfield(10);
//        final AtomicReferenceArray<Boolean> neighborBitfield = generateFullBitfield(10);
//
//        assertFalse(AtomicReferenceArrayHelper.isInterested(peerBitfield, neighborBitfield));
//    }
//
//    // TODO: abstract this out (repeated code)
//    private AtomicReferenceArray<Boolean> generateEmptyBitfield(final int size) {
//        return generateBitfield(size, false);
//    }
//
//    private AtomicReferenceArray<Boolean> generateFullBitfield(final int size) {
//        return generateBitfield(size, true);
//    }
//
//    private AtomicReferenceArray<Boolean> generateBitfield(final int size, final boolean value) {
//        final AtomicReferenceArray<Boolean> pieces = new AtomicReferenceArray<>(size);
//        for (int i = 0; i < size; i++) {
//            pieces.set(i, value);
//        }
//        return pieces;
//    }
//}
