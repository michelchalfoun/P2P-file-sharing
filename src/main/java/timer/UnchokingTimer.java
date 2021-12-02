package timer;

import neighbor.Neighbor;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class UnchokingTimer extends Thread {

    private Timer timer;
    private final Map<Integer, Neighbor> neighborData;
    private final int interval;

    public UnchokingTimer(final int intervalInSeconds, final Map<Integer, Neighbor> neighborData) {
        this.interval = intervalInSeconds;
        this.neighborData = neighborData;
        timer = new Timer();
    }

    public void run() {
        timer = new Timer();
        final TimerTask task = new UnchokeTask();
        final long intervalInMs = interval * 1000L;
        timer.schedule(task, intervalInMs, intervalInMs);
    }

    class UnchokeTask extends TimerTask {
        public void run() {
            // A -> B,C,D
            //      5,15,0
            //      0.5, 1.5, 0
            // interval = 10sec

            // B, C
            // B -> unchoke message
            // C -> unchoke message

            final Map<Neighbor, Float> downloadRatesByNeighbor =
                    neighborData.entrySet().stream()
                            .collect(Collectors.toMap(
                                            Map.Entry::getValue,
                                            entry ->
                                                    (float) entry.getValue().getNumberOfDownloadedBytes() / (float) interval));



            System.out.println("Unchoking Ran");
            System.out.println(neighborData);
        }
    }
}
