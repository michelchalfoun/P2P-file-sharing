package messages.payload;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * Converts AtomicReferenceArray<Boolean> to byte[] array (and also the reverse). This class helps
 * us with the conversion of the representation of bitfields in order to facilitate the logic to
 * compare bitfields
 */
public class BitfieldPayload {
    private int messageLength;
    private byte[] payload;
    private AtomicReferenceArray<Boolean> pieces;

    // Bits to bytes
    public BitfieldPayload(final AtomicReferenceArray<Boolean> pieces) {
        messageLength = (int) Math.ceil((float) pieces.length() / 8.0f);
        payload = new byte[messageLength];

        for (int i = 0; i < pieces.length(); i += 8) {
            StringBuilder bitArray = new StringBuilder();
            for (int j = Math.min(pieces.length() - i - 1, 8 - 1); j >= 0; j--) {
                bitArray.append(pieces.get(i + j).equals(true) ? '1' : '0');
            }
            int decimalBase = Integer.parseInt(bitArray.toString(), 2);
            payload[i / 8] = (byte) decimalBase;
        }
    }

    // Bytes to bits
    public BitfieldPayload(final int messageLength, byte[] payloadInBytes) {
        pieces = new AtomicReferenceArray<>(messageLength);
        System.out.println(pieces.length());
        for (int i = 0; i < payloadInBytes.length; i++) {
            String binary =
                    reverse(
                            String.format("%8s", Integer.toBinaryString(payloadInBytes[i] & 0xFF))
                                    .replace(" ", "0"));

            for (int j = 0; j < Math.min(messageLength - (i * 8), 8); j++) {
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

    public int getMessageLength() {
        return messageLength;
    }

    public byte[] getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "BitfieldPayload{"
                + "messageLength="
                + messageLength
                + ", payload="
                + Arrays.toString(payload)
                + ", pieces="
                + pieces
                + '}';
    }
}
