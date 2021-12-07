package neighbor;

import com.sun.jndi.toolkit.ctx.AtomicContext;
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
        Logging.getInstance().custom("Atempting to close all connections: " + isRunning.get());
        lock.writeLock().lock();
        if (isRunning.compareAndSet(true, false)) {
            neighborData.values().forEach(neighbor -> {
                Logging.getInstance().custom("CLOSING OUTPUT/INPUT STREAM FOR " + neighbor.getPeerID());
                try {
                    neighbor.getOutputStream().close();
                } catch (IOException e) {
//                    e.printStackTrace();
                }
                try {
                    neighbor.getInputStream().close();
                } catch (IOException e) {
//                    e.printStackTrace();
                }
                try {
                    neighbor.getSocket().close();
                } catch (IOException e) {
//                    e.printStackTrace();
                }
            });
        }
        lock.writeLock().unlock();
    }

    public ReentrantReadWriteLock.ReadLock getTerminationLock() {
        return lock.readLock();
    }
}
