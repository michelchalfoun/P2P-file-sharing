package timer;

import logging.Logging;
import messages.Message;
import messages.MessageFactory;
import messages.MessageType;
import messages.payload.PayloadFactory;
import neighbor.Neighbor;
import neighbor.DownloadRateContainer;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class UnchokingTimer {

    private long counter;

    private final int peerID;
    private Timer timer;
    private final Map<Integer, Neighbor> neighborData;
    private final int unchokingInterval;
    private final int optimisticallyUnchokingInterval;
    private final int numberOfPreferredNeighbors;

    final ThreadLocalRandom threadLocalRandom;

    private final MessageFactory messageFactory;
    private final PayloadFactory payloadFactory;

    private final Logging logger;
    final AtomicBoolean isRunning;

    public UnchokingTimer(final int peerID,
                          final int unchokingInterval,
                          final int optimisticallyUnchokingInterval,
                          final int numberOfPreferredNeighbors,
                          final Map<Integer, Neighbor> neighborData,
                          final AtomicBoolean isRunning) {
        this.peerID = peerID;
        this.unchokingInterval = unchokingInterval;
        this.optimisticallyUnchokingInterval = optimisticallyUnchokingInterval;
        this.neighborData = neighborData;
        this.numberOfPreferredNeighbors = numberOfPreferredNeighbors;
        this.isRunning = isRunning;
        timer = new Timer();
        counter = 0;
        messageFactory = new MessageFactory();
        payloadFactory = new PayloadFactory();

        threadLocalRandom = ThreadLocalRandom.current();
        logger = Logging.getInstance();
    }

    public void start() {
        timer = new Timer();
        final TimerTask task = new UnchokeTask();
        timer.schedule(task, 1000, 1000);
    }

    class UnchokeTask extends TimerTask {

        public void run() {
            if (!isRunning.get()) {
                timer.cancel();
                return;
            }

            if (counter % unchokingInterval == 0) {
                setUnchokedNeighbors();
            }

            if (counter % optimisticallyUnchokingInterval == 0) {
                setOptimisticallyUnchokedPeerNeighbor();
            }

            counter++;
        }

        private Set<Integer> getUnchokedPeerIDs() {
            final List<DownloadRateContainer> downloadRateContainers =
                    neighborData.entrySet().stream()
                            .collect(Collectors.toMap(
                                    Map.Entry::getValue,
                                    entry ->
                                            (float) entry.getValue().getNumberOfDownloadedBytes() / (float) unchokingInterval))
                            .entrySet().stream()
                            .map(entry -> new DownloadRateContainer(entry.getKey(), entry.getValue()))
                            .collect(Collectors.toList());
            Collections.sort(downloadRateContainers);

            final List<DownloadRateContainer> unchokedDownloadRateContainers =
                    downloadRateContainers.stream()
                            .filter(downloadRateContainer -> downloadRateContainer.getNeighbor().isInterested())
                            .limit(numberOfPreferredNeighbors)
                            .collect(Collectors.toList());

            final Set<Integer> unchokedPeerIDs =
                    unchokedDownloadRateContainers.stream()
                            .map(downloadRateContainer -> downloadRateContainer.getNeighbor().getPeerID())
                            .collect(Collectors.toSet());
            return unchokedPeerIDs;
        }

        private void setUnchokedNeighbors() {
            final Set<Integer> unchokedPeerIDs = getUnchokedPeerIDs();

            logger.changePreferredNeighbors(peerID, unchokedPeerIDs);

            neighborData.values().forEach(neighbor -> {
                if (!neighbor.isChoked() && !unchokedPeerIDs.contains(neighbor.getPeerID())) {
                    // Neighbor has been choked (previously unchoked)
                    sendChokeMessage(true, neighbor);
                    neighbor.setChoked(true);
                } else if (neighbor.isChoked() && unchokedPeerIDs.contains(neighbor.getPeerID())) {
                    // Neighbor has been unchoked (previously choked)
                    sendChokeMessage(false, neighbor);
                    neighbor.setChoked(false);
                }
            });
        }

        private void setOptimisticallyUnchokedPeerNeighbor(){
            final List<Integer> chokedPeerIDs =
                    neighborData.values().stream()
                            .filter(Neighbor::isChoked)
                            .map(Neighbor::getPeerID)
                            .collect(Collectors.toList());

            if (chokedPeerIDs.size() != 0){
                final int randomNeighbor = chokedPeerIDs.get(threadLocalRandom.nextInt(0, chokedPeerIDs.size()));
                neighborData.get(randomNeighbor).setChoked(false);
                logger.changeOptimUnchokedNeighbor(peerID, randomNeighbor);
                sendChokeMessage(false, neighborData.get(randomNeighbor));
            }
        }

        private void sendChokeMessage(final boolean isChoked, final Neighbor neighbor) {
            final Message request =
                    messageFactory.createMessage(isChoked ? MessageType.CHOKE : MessageType.UNCHOKE);

            if (!isRunning.get()) {
                timer.cancel();
                return;
            }

            neighbor.sendMessageInOutputStream(request);
        }
    }
}
