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

    private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
//        byte one = aInputStream.readByte();
//        System.out.println("One: " + one);
//        byte two = aInputStream.readByte();
//        System.out.println("Two: " + two);
//        byte three = aInputStream.readByte();
//        System.out.println("Three: " + three);
//        byte four = aInputStream.readByte();
//        System.out.println("Four: " + four);
//
////
//        messageLength = new util.IntBytes(new byte[]{one, two, three, four}).getIntValue();
//
        messageLength = aInputStream.readInt();
        messageType = aInputStream.readByte();

        System.out.println("Message length: " + messageLength + " and message type: " + MessageType.values()[messageType]);

        payloadBytes = new byte[messageLength - 1];

        for (int i = 0; i < messageLength - 1; i++) {
            payloadBytes[i] = aInputStream.readByte();
        }
    }

    private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
        aOutputStream.writeInt(messageLength);
        aOutputStream.write(messageType);
        aOutputStream.write(payloadBytes);

        System.out.println("*SENT*: message type: " + MessageType.values()[messageType] + " at " + System.currentTimeMillis());
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
