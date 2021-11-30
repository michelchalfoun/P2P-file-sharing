package messages.payload;

import messages.Message;

import java.util.concurrent.atomic.AtomicReferenceArray;

public class PayloadFactory
{
    public BitfieldPayload createBitfieldPayload(final AtomicReferenceArray<Boolean> pieces) {
        return new BitfieldPayload(pieces);
    }

    public BitfieldPayload createBitfieldPayload(final Message message) {
        return new BitfieldPayload(message.getPayloadLength(), message.getPayloadBytes());
    }
}
