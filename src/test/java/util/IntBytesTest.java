package util;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class IntBytesTest {
    @Test
    public void testLengthOfInteger() {
        assertEquals(4, new IntBytes(1).getBytes().length);
        assertEquals(4, new IntBytes(7236).getBytes().length);
        assertEquals(4, new IntBytes(99999999).getBytes().length);
    }

    @Test
    public void testByteToIntTranslation() {
        final int intValue = 723663;
        byte[] bytes = new IntBytes(intValue).getBytes();

        assertEquals(intValue, new IntBytes(bytes).getIntValue());
    }
}
