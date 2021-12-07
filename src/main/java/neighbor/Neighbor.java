package neighbor;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Objects;

import messages.Message;
import messages.HandshakeMessage;
import pieces.Pieces;

public class Neighbor {
    private final Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    private final int peerID;
    private Pieces pieces;

    private boolean isChoked;

    private boolean interested;
    private int numberOfDownloadedBytes;

    public Neighbor(final Socket socket, final int peerID) {
        this.socket = socket;
        this.peerID = peerID;
        this.isChoked = true;
        this.interested = false;
    }

    public Neighbor(final Socket socket, final int peerID, final ObjectInputStream inputStream, final ObjectOutputStream outputStream, final int numberOfPieces) {
        this(socket, peerID);
        this.outputStream = outputStream;
        this.inputStream = inputStream;
        this.pieces = new Pieces(numberOfPieces);
    }

    public synchronized int getPeerID() {
        return peerID;
    }

    public synchronized Pieces getPieces() {
        return pieces;
    }

    public synchronized void setPieces(final Pieces pieces) {
        this.pieces = pieces;
    }

    public synchronized Socket getSocket() {
        return socket;
    }

    public synchronized boolean isChoked() {
        return isChoked;
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

    public synchronized void setInterested(final boolean interested) {
        this.interested = interested;
    }

    public synchronized void setChoked(boolean choked) {
        this.isChoked = choked;
    }

    public ObjectOutputStream getOutputStream() {
        return outputStream;
    }

    public ObjectInputStream getInputStream() {
        return inputStream;
    }

    public synchronized void sendMessageInOutputStream(final Message message) {
        try {
            outputStream.writeObject(message);
            outputStream.flush();
        } catch (java.io.IOException e) {
//            e.printStackTrace();
        }
    }

    public synchronized void sendMessageInOutputStream(final HandshakeMessage message) {
        try {
            outputStream.writeObject(message);
            outputStream.flush();
        } catch (java.io.IOException e) {
//            e.printStackTrace();
        }
    }

    public synchronized void setOutputStream(final ObjectOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public String toString() {
        return "Neighbor{" +
                "peerID=" + peerID +
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
