package peer;

public class PeerMetadata
{
    private final int peerID;
    private final String hostName;
    private final int listeningPort;
    private final boolean hasFile;

    public PeerMetadata(final int peerID, final String hostName, final int listeningPort, final boolean hasFile) {
        this.peerID = peerID;
        this.hostName = hostName;
        this.listeningPort = listeningPort;
        this.hasFile = hasFile;
    }

    public int getPeerID() {
        return peerID;
    }

    public String getHostName() {
        return hostName;
    }

    public int getListeningPort() {
        return listeningPort;
    }

    public boolean isHasFile() {
        return hasFile;
    }

    @Override
    public String toString() {
        return "PeerMetadata{" +
                "peerID=" + peerID +
                ", hostName='" + hostName + '\'' +
                ", listeningPort=" + listeningPort +
                ", hasFile=" + hasFile +
                '}';
    }
}
