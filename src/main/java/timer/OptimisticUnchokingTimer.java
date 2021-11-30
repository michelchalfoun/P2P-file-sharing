package timer;

import java.util.Timer;
import java.util.TimerTask;

public class OptimisticUnchokingTimer extends Thread {

    private Timer timer;
    private final int interval;

    public OptimisticUnchokingTimer(int seconds) {
        interval = seconds;
        timer = new Timer();
    }

    public void run() {
        timer = new Timer();
        TimerTask task = new timer.OptimisticUnchokingTimer.OptimisticUnchokingTask();
        timer.schedule(task, 0, interval * 1000);
    }

    class OptimisticUnchokingTask extends TimerTask {
        public void run() {
            System.out.println("Optimistic Unchoking Ran");
        }
    }
}
