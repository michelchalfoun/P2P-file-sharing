package logging;

import org.junit.Test;

public class LoggingTest {
    @Test
    public void testLogger() {
        int i = 9000;
        Logging logger = Logging.getInstance();
        logger.setPeerID(i);
        logger.TCP_receive(i, 1002);
        logger.receiveHaveMsg(i, 1000, 1);
        logger.receiveInterestedMsg(i, 2);
        logger.receiveInterestedMsg(i, 2);
        logger.receiveInterestedMsg(i, 2);
    }
}
