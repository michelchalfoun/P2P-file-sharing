package util;

public class Tst {
    boolean isRunning = true;

    public Tst() {

        final long target = System.currentTimeMillis() + 5 * 1000;
        Thread threadOne = new Thread() {
            public void run() {
                while (isRunning) System.out.println("Thread Running");
            }
        };
        threadOne.start();

        Thread threadTwo = new Thread() {
            public void run() {
                while (isRunning) {
                    if (System.currentTimeMillis() >= target) {
                        isRunning = false;
                        threadOne.interrupt();
                    }
                }
            }
        };
        threadTwo.start();
    }

    public static void main(String[] args) {
        new Tst();
    }
}
