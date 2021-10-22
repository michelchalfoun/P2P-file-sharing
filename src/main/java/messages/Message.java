package messages;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

public class Message implements Serializable {
    private int messageLength;
    private byte messageType;
    private byte[] messagePayload;

    public Message(final int messageLength, final int messageType, final byte[] messagePayload) {
        this.messageLength = messageLength;
        this.messageType = (byte) messageType;
        this.messagePayload = messagePayload;
    }

    private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
        messageLength = aInputStream.readInt();
        messageType = aInputStream.readByte();

        messagePayload = new byte[messageLength - 1];
        for (int i = 0; i < messageLength - 1; i++) {
            messagePayload[i] = aInputStream.readByte();
        }
    }

    private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
        aOutputStream.writeInt(messageLength);
        aOutputStream.write(messageType);
        aOutputStream.write(messagePayload);
    }

    public MessageType getMessageType() {
        return MessageType.getMessageType(messageType);
    }

    public int getMessageLength() {
        return messageLength;
    }

    public byte[] getMessagePayload() {
        return messagePayload;
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageLength=" + messageLength +
                ", messageType=" + messageType +
                ", messagePayload=" + Arrays.toString(messagePayload) +
                '}';
    }
}
