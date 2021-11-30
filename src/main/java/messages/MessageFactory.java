package messages;

import messages.payload.Payload;

public class MessageFactory {
    public Message createMessage(final Payload payload, final MessageType messageType) {
        return new Message(payload.getBytes().length, messageType.getValue(), payload.getBytes());
    }
}
