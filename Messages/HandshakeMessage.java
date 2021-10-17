package Messages;

import java.nio.charset.StandardCharsets;

/*

Then the peer process starts and reads the file Common.cfg to set the corresponding
variables. The peer process also reads the file PeerInfo.cfg. It will find that the [has file
or not] field is 1, which means it has the complete file, it sets all the bits of its bitfield to
be 1. (On the other hand, if the [has file or not] field is 0, it sets all the bits of its bitfield
to 0.) Here, the bitfield is a data structure where your peer process manages the pieces.
You have the freedom in how to implement it. This peer also finds out that it is the first
peer; it will just listen on the port 6008 as sp

 */

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
