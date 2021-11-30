package timer;

import neighbor.Neighbor;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class UnchokingTimer extends Thread{

    private Timer timer;
    private final Map<Integer, Neighbor> neighborData;
    private final int interval;

    public UnchokingTimer(int seconds, Map<Integer, Neighbor> neighborData) {
        interval = seconds;
        timer = new Timer();
        this.neighborData = neighborData;
    }

    public void run() {
        timer = new Timer();
        TimerTask task = new UnchokeTask();
        timer.schedule(task, 0, interval * 1000);
    }

    class UnchokeTask extends TimerTask {
//        public int i = 0;
        public void run() {
            System.out.println("Unchoking Ran");
            System.out.println(neighborData);
        }
    }

}
