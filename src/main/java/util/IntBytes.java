package util;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class IntBytes {
    private byte[] bytes;
    private int intValue;

    public IntBytes(final int intValue) {
        this.intValue = intValue;
        try {
            bytes = intToBytes(intValue);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public IntBytes(final byte[] bytes) {
        this.bytes = bytes;
        this.intValue = bytesToInt(bytes);
    }

    // TODO
    // Build tests for this
    private byte[] intToBytes(int my_int) throws IOException {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(my_int).array();
    }

    public int bytesToInt(byte[] int_bytes) {
        return ByteBuffer.wrap(int_bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    public byte[] getBytes() {
        return bytes;
    }

    public int getIntValue() {
        return intValue;
    }
}
