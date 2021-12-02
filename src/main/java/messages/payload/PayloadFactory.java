package messages.payload;

import messages.Message;
import messages.payload.impl.BitfieldPayload;
import messages.payload.impl.PieceIndexPayload;
import messages.payload.impl.PiecePayload;

import java.util.concurrent.atomic.AtomicReferenceArray;

public class PayloadFactory
{
    public BitfieldPayload createBitfieldPayload(final AtomicReferenceArray<Boolean> pieces) {
        return new BitfieldPayload(pieces);
    }

    public BitfieldPayload createBitfieldPayload(final Message message, final int numberOfPieces) {
        return new BitfieldPayload(numberOfPieces, message.getPayloadBytes());
    }

    public PieceIndexPayload createPieceIndexPayload(final int pieceID) {
        return new PieceIndexPayload(pieceID);
    }

    public PieceIndexPayload createPieceIndexPayload(final Message message) {
        return new PieceIndexPayload(message.getPayloadBytes());
    }

    public PiecePayload createPiecePayload(final Message message) {
        return new PiecePayload(message.getPayloadBytes());
    }

    public PiecePayload createPiecePayload(final int pieceID, final byte[] pieceBytes) {
        return new PiecePayload(pieceID, pieceBytes);
    }

}
