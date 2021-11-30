package neighbor;

import java.net.Socket;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class Neighbor {
    private final int peerID;
    private final Socket socket;
    private AtomicReferenceArray bitfield;
    private boolean choke;

    private boolean interested;
    private float downloadRate;

    public Neighbor(final Socket socket, final int peerID) {
        this.socket = socket;
        this.peerID = peerID;
    }

    public synchronized int getPeerID() {
        return peerID;
    }

    // TODO: check if you need this
    public synchronized AtomicReferenceArray getBitfield() {
        return bitfield;
    }

    public synchronized Socket getSocket() {
        return socket;
    }

    public synchronized boolean getChoke() {
        return choke;
    }

    public synchronized boolean isInterested() {
        return interested;
    }

    public synchronized float getDownloadRate() {
        return downloadRate;
    }

    public synchronized void setBitfield(final AtomicReferenceArray bitfield) {
        this.bitfield = bitfield;
    }

    public synchronized void setInterested(final boolean interested) {
        this.interested = interested;
    }

    public synchronized void setChoke(boolean choke) {
        this.choke = choke;
    }

    @Override
    public String toString() {
        return "Neighbor{" +
                "peerID=" + peerID +
                ", bitfield=" + bitfield +
                ", interested=" + interested +
                ", downloadRate=" + downloadRate +
                '}';
    }
}
