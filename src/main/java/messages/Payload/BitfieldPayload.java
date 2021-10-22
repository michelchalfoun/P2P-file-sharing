package messages.Payload;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class BitfieldPayload {
    private int messageLength;
    private byte[] payload;
    private AtomicReferenceArray<Boolean> pieces;

    @Override
    public String toString() {
        return "BitfieldPayload{" +
                "messageLength=" + messageLength +
                ", payload=" + Arrays.toString(payload) +
                ", pieces=" + pieces +
                '}';
    }

    // Bits to bytes
    public BitfieldPayload(final AtomicReferenceArray<Boolean> pieces) {
        messageLength = (int) Math.ceil((float) pieces.length() / 8.0f);
        payload = new byte[messageLength];

        System.out.println(pieces);

        for (int i = 0; i < pieces.length(); i += 8) {
            StringBuilder bitArray = new StringBuilder();
            for (int j = Math.min(pieces.length() - i - 1, 8 - 1) ; j >= 0; j--) {
                bitArray.append(pieces.get(i + j).equals(true) ? '1' : '0');
            }
            int decimalBase = Integer.parseInt(bitArray.toString(), 2);
            payload[i / 8] = (byte) decimalBase;
        }
    }

    // Bytes to bits
    public BitfieldPayload(final int messageLength, byte[] payloadInBytes) {
        // 00010000
        // toBinaryString -> 00001000
        pieces = new AtomicReferenceArray<>(messageLength);
        for (int i = 0; i < payloadInBytes.length; i++) {
            // String.format("%16s", Integer.toBinaryString(1)).replace(" ", "0")
            String binary = String.format("%8s", Integer.toBinaryString(payloadInBytes[i] & 0xFF)).replace(" ", "0");
            System.out.println(binary);

            for (int j = binary.length() - 1; j >= 0; j--) {

                pieces.set((8 * (i + 1)) - j - 1, binary.charAt(j) == '1');
            }
        }
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
}
