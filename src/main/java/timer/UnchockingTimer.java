package timer;

import java.util.Timer;
import java.util.TimerTask;

public class UnchokingTimer extends Thread{

    Timer timer;

    public UnchokingTimer() {
        timer = new Timer();
    }

    public void setTime(int seconds) {
        this.timer.schedule(new UnchokeTask(), seconds * 1000);
    }

    public void run() {

    }

    class UnchokeTask extends TimerTask {
        public static int i = 0;
        public void run() {
            System.out.println("Timer ran" + ++i);
            if (i % )
        }
    }

}
