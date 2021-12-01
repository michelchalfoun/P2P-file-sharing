package messages.payload;

import messages.Message;
import messages.payload.impl.BitfieldPayload;
import messages.payload.impl.PieceIndexPayload;

import java.util.concurrent.atomic.AtomicReferenceArray;

public class PayloadFactory
{
    public BitfieldPayload createBitfieldPayload(final AtomicReferenceArray<Boolean> pieces) {
        return new BitfieldPayload(pieces);
    }

    public BitfieldPayload createBitfieldPayload(final Message message, final int numberOfPieces) {
        return new BitfieldPayload(numberOfPieces, message.getPayloadBytes());
    }

    public PieceIndexPayload createHavePayload(final int pieceId) {
        return new PieceIndexPayload(pieceId);
    }

    public PieceIndexPayload createHavePayload(final Message message) {
        return new PieceIndexPayload(message.getPayloadBytes());
    }
}
