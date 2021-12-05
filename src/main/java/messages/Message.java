package messages;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

/**
 * General message that will be sent from peer to peer (there are 8 different types of messages that
 * are specified in MessageType)
 */
public class Message
        implements Serializable
{
    private int messageLength;
    private byte messageType;
    private byte[] payloadBytes;

    public Message(final int payloadLength, final int messageType, final byte[] payloadBytes) {
        this.messageLength = payloadLength + 1; // Adds one to account for the message type byte
        this.messageType = (byte) messageType;
        this.payloadBytes = payloadBytes;
    }

    public Message(final int messageType) {
        this.messageLength = 1; // Adds one to account for the message type byte
        this.messageType = (byte) messageType;
        this.payloadBytes = new byte[] {};
    }

    private void readObject(final ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
        messageLength = aInputStream.readInt();
        messageType = aInputStream.readByte();

        payloadBytes = new byte[messageLength - 1];

        for (int i = 0; i < messageLength - 1; i++) {
            payloadBytes[i] = aInputStream.readByte();
        }
    }

    private void writeObject(final ObjectOutputStream aOutputStream) throws IOException {
        aOutputStream.writeInt(messageLength);
        aOutputStream.write(messageType);
        aOutputStream.write(payloadBytes);
    }

    public MessageType getMessageType() {
        return MessageType.getMessageType(messageType);
    }

    public int getPayloadLength() {
        return messageLength - 1;
    }

    public byte[] getPayloadBytes() {
        return payloadBytes;
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageLength=" + messageLength +
                ", messageType=" + MessageType.values()[messageType].name() +
                ", messagePayload=" + Arrays.toString(payloadBytes) +
                '}';
    }
}
