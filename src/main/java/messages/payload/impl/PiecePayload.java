package messages.payload.impl;

import messages.payload.Payload;

public class PiecePayload implements Payload {

    private byte[] payload;

    public PiecePayload(final byte[] payload) {
        this.payload = payload;
    }

    @Override
    public byte[] getBytes() {
        return new byte[0];
    }
}
