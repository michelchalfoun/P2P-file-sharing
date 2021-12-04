package messages.payload.impl;

import org.junit.Test;
import util.IntBytes;

import java.util.Arrays;

public class PiecePayloadTest {
    @Test
    public void testSerializingPayload() {
        final String text = "test string";
        final byte[] textBytes = text.getBytes();
        final int pieceID = 5;
        final byte[] pieceIDBytes = new IntBytes(pieceID).getBytes();

        PiecePayload piecePayload = new PiecePayload(5, textBytes);

        byte[] expected = new byte[4 + textBytes.length];
        for (int i = 0; i < 4; i++) expected[i] = pieceIDBytes[i];
        for (int i = 0; i < textBytes.length; i++) expected[i + 4] = textBytes[i];
    }

    public static void main(String[] args) {
        String s = "test string";
        byte[] b = s.getBytes();

        byte[] lol = new IntBytes(2).getBytes();

        System.out.println(new IntBytes(lol).getIntValue());

        System.out.println(Arrays.toString(new IntBytes(2).getBytes()));
    }
}
