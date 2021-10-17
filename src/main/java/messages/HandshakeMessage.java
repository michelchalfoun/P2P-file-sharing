package messages;

public class HandshakeMessage
{
    private final static String HANDSHAKE_HEADER = "P2PFILESHARINGPROJ";
    private final static byte[] ZERO_BITS = new byte[10];
    private final int peerID;

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
}
