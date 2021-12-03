package neighbor;

import java.util.Objects;

public class DownloadRateContainer implements Comparable {

    private final float downloadRate;
    private final Neighbor neighbor;

    public DownloadRateContainer(final Neighbor neighbor, final float downloadRate) {
        this.downloadRate = downloadRate;
        this.neighbor = neighbor;
    }

    public float getDownloadRate() {
        return downloadRate;
    }

    public Neighbor getNeighbor() {
        return neighbor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DownloadRateContainer that = (DownloadRateContainer) o;
        return Float.compare(that.downloadRate, downloadRate) == 0;
    }

    @Override
    public int hashCode() {
        return neighbor.hashCode();
    }


    @Override
    public int compareTo(Object o) {
        if (this == o) return 0;
        if (o == null || getClass() != o.getClass()) return -1;
        DownloadRateContainer that = (DownloadRateContainer) o;
        return downloadRate > that.downloadRate ? -1 : 1;
    }

    @Override
    public String toString() {
        return "DownloadRateContainer{" +
                "downloadRate=" + downloadRate +
                ", neighbor=" + neighbor +
                '}';
    }
}
