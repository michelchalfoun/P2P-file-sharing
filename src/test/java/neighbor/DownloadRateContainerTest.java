package neighbor;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static junit.framework.Assert.assertEquals;

public class DownloadRateContainerTest {

    private int peerID;

    public DownloadRateContainerTest() {
        peerID = 0;
    }

    @Test
    public void testSortingNeighborWithDownloadingRate() {
        final DownloadRateContainer firstNeighbor =
                new DownloadRateContainer(buildTestNeighbor(), 5.0F);
        final DownloadRateContainer secondNeighbor =
                new DownloadRateContainer(buildTestNeighbor(), 2.0F);
        final DownloadRateContainer thirdNeighbor =
                new DownloadRateContainer(buildTestNeighbor(), 0.5F);

        final List<DownloadRateContainer> neighbors =
                Arrays.asList(firstNeighbor, secondNeighbor, thirdNeighbor);
        final List<Float> expected = Arrays.asList(5.0F, 2.0F, 0.5F);

        Collections.sort(neighbors);

        assertEquals(
                expected,
                neighbors.stream()
                        .map(DownloadRateContainer::getDownloadRate)
                        .collect(Collectors.toList()));

        assertEquals(
                Arrays.asList(5.0F, 2.0F),
                neighbors.stream()
                        .map(DownloadRateContainer::getDownloadRate)
                        .limit(2)
                        .collect(Collectors.toList()));
    }

    private Neighbor buildTestNeighbor() {
        return new Neighbor(null, peerID++);
    }
}
