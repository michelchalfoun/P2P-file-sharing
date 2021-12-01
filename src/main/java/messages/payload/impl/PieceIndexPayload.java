package messages.payload.impl;

import messages.payload.Payload;
import util.IntBytes;

import java.io.*;

public class PieceIndexPayload implements Payload {

    private final int pieceID;

    public PieceIndexPayload(final int pieceID) {
        this.pieceID = pieceID;
    }

    public PieceIndexPayload(final byte[] payloadInBytes) {
        pieceID = new IntBytes(payloadInBytes).getIntValue();
    }

    @Override
    public byte[] getBytes() {
        return new IntBytes(pieceID).getBytes();
    }

    public int getPieceID() {
        return pieceID;
    }
}
