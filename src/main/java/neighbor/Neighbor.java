package neighbor;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class Neighbor {
    private final Socket socket;
    private ObjectOutputStream outputStream;

    private final int peerID;
    private AtomicReferenceArray bitfield;
    private boolean choke;

    private boolean interested;
    private int numberOfDownloadedBytes;

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

    public synchronized int getNumberOfDownloadedBytes() {
        return numberOfDownloadedBytes;
    }

    public synchronized void addNumberOfDownloadedBytes(final int numberOfBytes) {
        numberOfDownloadedBytes += numberOfBytes;
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

    public synchronized ObjectOutputStream getOutputStream() {
        return outputStream;
    }

    public synchronized void setOutputStream(final ObjectOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public String toString() {
        return "Neighbor{" +
                "peerID=" + peerID +
                ", bitfield=" + bitfield +
                ", interested=" + interested +
                ", downloadRate=" + numberOfDownloadedBytes +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Neighbor neighbor = (Neighbor) o;
        return peerID == neighbor.peerID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(peerID);
    }
}
