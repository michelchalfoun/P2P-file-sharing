package messages;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class HandshakeMessage implements Serializable {
    private String HANDSHAKE_HEADER = "P2PFILESHARINGPROJ";
    private byte[] ZERO_BITS = new byte[10];
    private int peerID;

    public HandshakeMessage(final int peerID) {
        this.peerID = peerID;
    }

    public String getHandshakeHeader() {
        return HANDSHAKE_HEADER;
    }

    public byte[] getZeroBits() {
        return ZERO_BITS;
    }

    public int getPeerID() {
        return peerID;
    }

    private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
        HANDSHAKE_HEADER = aInputStream.readUTF();
        for (int i = 0; i < 10; i++) aInputStream.readByte();
        ZERO_BITS = new byte[10];
        peerID = aInputStream.readInt();
    }

    private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
        aOutputStream.writeUTF(HANDSHAKE_HEADER);
        aOutputStream.write(ZERO_BITS);
        aOutputStream.writeInt(peerID);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HandshakeMessage that = (HandshakeMessage) o;
        return peerID == that.peerID && HANDSHAKE_HEADER.equals(that.HANDSHAKE_HEADER) && Arrays.equals(ZERO_BITS, that.ZERO_BITS);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(HANDSHAKE_HEADER, peerID);
        result = 31 * result + Arrays.hashCode(ZERO_BITS);
        return result;
    }

    @Override
    public String toString() {
        return "HandshakeMessage{" +
                "HANDSHAKE_HEADER='" + HANDSHAKE_HEADER + '\'' +
                ", ZERO_BITS=" + Arrays.toString(ZERO_BITS) +
                ", peerID=" + peerID +
                '}';
    }
}
