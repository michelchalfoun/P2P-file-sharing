package messages.payload.impl;

import messages.payload.Payload;
import util.IntBytes;

public class PiecePayload implements Payload {

    private final byte[] payload;
    private final int pieceID;

    public PiecePayload(final int pieceID, final byte[] piecePayload) {
        this.pieceID = pieceID;
        payload = new byte[piecePayload.length + 4];
        byte[] pieceIDBytes = new IntBytes(pieceID).getBytes();

        if (pieceIDBytes.length != 4) {
            System.out.println("piece ID bytes should be 4");
        }

        for (int index = 0; index < 4; index++) {
            payload[index] = pieceIDBytes[index];
        }
        for (int index = 0; index < piecePayload.length; index++) {
            payload[index + 4] = piecePayload[index];
        }
    }

    public PiecePayload(final byte[] messagePayload) {
        byte[] pieceIDBytes = new byte[4];
        payload = new byte[messagePayload.length - 4];
        for (int index = 0; index < 4; index++) {
            pieceIDBytes[index] = messagePayload[index];
        }
        for (int index = 4; index < messagePayload.length; index++) {
            payload[index - 4] = messagePayload[index];
        }
        pieceID = new IntBytes(pieceIDBytes).getIntValue();
    }

    @Override
    public byte[] getBytes() {
        return payload;
    }

    public byte[] getPayload() {
        return payload;
    }

    public int getPieceID() {
        return pieceID;
    }
}
