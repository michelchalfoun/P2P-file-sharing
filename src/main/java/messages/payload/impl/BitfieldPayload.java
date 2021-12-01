package messages.payload.impl;

import messages.payload.Payload;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * Converts AtomicReferenceArray<Boolean> to byte[] array (and also the reverse). This class helps
 * us with the conversion of the representation of bitfields in order to facilitate the logic to
 * compare bitfields
 */
public class BitfieldPayload implements Payload {
    private byte[] payload;
    private AtomicReferenceArray<Boolean> pieces;

    // Bits to bytes
    public BitfieldPayload(final AtomicReferenceArray<Boolean> pieces) {
        final int payloadLength = (int) Math.ceil(pieces.length() / 8.0f);
        payload = new byte[payloadLength];

        for (int i = 0; i < pieces.length(); i += 8) {
            final StringBuilder bitArray = new StringBuilder();
            for (int j = Math.min(pieces.length() - i - 1, 8 - 1); j >= 0; j--) {
                bitArray.append(pieces.get(i + j).equals(true) ? '1' : '0');
            }
            int decimalBase = Integer.parseInt(bitArray.toString(), 2);
            payload[i / 8] = (byte) decimalBase;
        }
    }

    // Bytes to bits
    public BitfieldPayload(final int numberOfPieces, byte[] payloadInBytes) {
        System.out.println("PAYLOAD BYTES SIZE: " + payloadInBytes.length);
        pieces = new AtomicReferenceArray<>(numberOfPieces);
        for (int i = 0; i < payloadInBytes.length; i++) {
            String binary =
                    reverse(
                            String.format("%8s", Integer.toBinaryString(payloadInBytes[i] & 0xFF))
                                    .replace(" ", "0"));

            for (int j = 0; j < Math.min(numberOfPieces - (i * 8), 8); j++) {
                int index = (i * 8) + j;
                pieces.set(index, binary.charAt(j) == '1');
            }
        }
    }

    private String reverse(String a) {
        int j = a.length();
        char[] newWord = new char[j];
        for (int i = 0; i < a.length(); i++) {
            newWord[--j] = a.charAt(i);
        }
        return new String(newWord);
    }

    public AtomicReferenceArray<Boolean> getPieces() {
        return pieces;
    }

    @Override
    public String toString() {
        return "BitfieldPayload{"
                + ", payload="
                + Arrays.toString(payload)
                + ", pieces="
                + pieces
                + '}';
    }

    @Override
    public byte[] getBytes() {
        return this.payload;
    }
}
