package neighbor;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class NeighborDataWrapper {
    private final ReentrantReadWriteLock lock;
    private final Map<Integer, Neighbor> neighborData;
    private final AtomicBoolean isRunning;

    public NeighborDataWrapper(final AtomicBoolean isRunning) {
        neighborData = new ConcurrentHashMap<>();
        lock = new ReentrantReadWriteLock();
        this.isRunning = isRunning;
    }

    public Map<Integer, Neighbor> getNeighborData() {
        return neighborData;
    }

    public void closeAllConnections() {
        lock.writeLock().lock();
        if (isRunning.compareAndSet(true, false)) {
            neighborData.values().forEach(neighbor -> {
                try {
                    neighbor.getOutputStream().close();
                } catch (IOException e) {
                }
                try {
                    neighbor.getInputStream().close();
                } catch (IOException e) {
                }
                try {
                    neighbor.getSocket().close();
                } catch (IOException e) {
                }
            });
        }
        lock.writeLock().unlock();
    }
}
