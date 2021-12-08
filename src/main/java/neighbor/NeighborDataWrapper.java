package neighbor;

import logging.Logging;

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
            Logging.getInstance().custom("Started closing streams");
            neighborData.values().forEach(neighbor -> {
                Logging.getInstance().custom("Closing stream for neighbor " + neighbor.getPeerID());
                try {
                    neighbor.getOutputStream().close();
                } catch (IOException e) {
                    Logging.getInstance().customErr(e);
                }
                try {
                    neighbor.getInputStream().close();
                } catch (IOException e) {
                    Logging.getInstance().customErr(e);
                }
                try {
                    neighbor.getSocket().close();
                } catch (IOException e) {
                    Logging.getInstance().customErr(e);
                }
            });
            Logging.getInstance().custom("Finished closing streams");
        }
        lock.writeLock().unlock();
    }

    public ReentrantReadWriteLock.ReadLock getTerminationLock() {
        return lock.readLock();
    }
}
