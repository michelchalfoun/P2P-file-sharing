package messages.payload.impl;

import messages.payload.Payload;
import pieces.Pieces;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * Converts AtomicReferenceArray<Boolean> to byte[] array (and also the reverse). This class helps
 * us with the conversion of the representation of bitfields in order to facilitate the logic to
 * compare bitfields
 */
public class BitfieldPayload implements Payload {
    private byte[] payload;
    private Pieces pieces;

    // Bits to bytes
    public BitfieldPayload(final Pieces pieces) {
        final int numberOfPieces = pieces.getNumberOfPieces();
        final int payloadLength = (int) Math.ceil(numberOfPieces / 8.0f);
        payload = new byte[payloadLength];

        for (int i = 0; i < numberOfPieces; i += 8) {
            final StringBuilder bitArray = new StringBuilder();
            for (int j = Math.min(numberOfPieces - i - 1, 8 - 1); j >= 0; j--) {
                bitArray.append(pieces.hasPiece(i + j) ? '1' : '0');
            }
            int decimalBase = Integer.parseInt(bitArray.toString(), 2);
            payload[i / 8] = (byte) decimalBase;
        }
    }

    // Bytes to bits
    public BitfieldPayload(final int numberOfPieces, byte[] payloadInBytes) {
        final AtomicReferenceArray<Boolean> bitfield = new AtomicReferenceArray<>(numberOfPieces);
        int numberOfPiecesDownloaded = 0;
        for (int i = 0; i < payloadInBytes.length; i++) {
            String binary =
                    reverse(
                            String.format("%8s", Integer.toBinaryString(payloadInBytes[i] & 0xFF))
                                    .replace(" ", "0"));

            for (int j = 0; j < Math.min(numberOfPieces - (i * 8), 8); j++) {
                int index = (i * 8) + j;
                if (binary.charAt(j) == '1') {
                    bitfield.set(index, true);
                    numberOfPiecesDownloaded++;
                } else {
                    bitfield.set(index, false);
                }
            }
        }
        pieces = new Pieces(bitfield, numberOfPiecesDownloaded);
    }

    private String reverse(String a) {
        int j = a.length();
        char[] newWord = new char[j];
        for (int i = 0; i < a.length(); i++) {
            newWord[--j] = a.charAt(i);
        }
        return new String(newWord);
    }

    public Pieces getPieces() {
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
