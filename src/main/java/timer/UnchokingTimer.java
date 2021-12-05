package timer;

import logging.Logging;
import messages.Message;
import messages.MessageFactory;
import messages.MessageType;
import messages.payload.PayloadFactory;
import neighbor.Neighbor;
import neighbor.DownloadRateContainer;
import logging.Logging;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class UnchokingTimer extends Thread {

    private final int peerID;
    private Timer timer;
    private final Map<Integer, Neighbor> neighborData;
    private final int interval;
    private final int numberOfPreferredNeighbors;

    private final MessageFactory messageFactory;
    private final PayloadFactory payloadFactory;

    private final Logging logger;

    public UnchokingTimer(final int peerID, final int intervalInSeconds, final int numberOfPreferredNeighbors, final Map<Integer, Neighbor> neighborData) {
        this.peerID = peerID;
        this.interval = intervalInSeconds;
        this.neighborData = neighborData;
        this.numberOfPreferredNeighbors = numberOfPreferredNeighbors;
        timer = new Timer();
        messageFactory = new MessageFactory();
        payloadFactory = new PayloadFactory();

        this.logger = new Logging();
    }

    public void run() {
        timer = new Timer();
        final TimerTask task = new UnchokeTask();
        final long intervalInMs = interval * 1000L;
        timer.schedule(task, intervalInMs, intervalInMs);
    }

    class UnchokeTask extends TimerTask {
        public void run() {
            final List<DownloadRateContainer> downloadRateContainers =
                    neighborData.entrySet().stream()
                            .collect(Collectors.toMap(
                                            Map.Entry::getValue,
                                            entry ->
                                                    (float) entry.getValue().getNumberOfDownloadedBytes() / (float) interval))
                            .entrySet().stream()
                            .map(entry -> new DownloadRateContainer(entry.getKey(), entry.getValue()))
                            .collect(Collectors.toList());
            Collections.sort(downloadRateContainers);

            final List<DownloadRateContainer> unchokedDownloadRateContainers =
                    downloadRateContainers.stream()
                            .filter(downloadRateContainer -> downloadRateContainer.getNeighbor().isInterested())
                            .limit(numberOfPreferredNeighbors)
                            .collect(Collectors.toList());

            consolidateUnchokedNeighbors(unchokedDownloadRateContainers);
        }

        /**
         *
         * Peer 1001
         * neighbors = [(1002, false)]
         *
         * Peer 1002
         * neighbors = [(1001, false)]
         *
         */

        /**
         *
         */

        private void consolidateUnchokedNeighbors(
                final List<DownloadRateContainer> unchokedDownloadRateContainers) {
            final Set<Integer> unchokedPeerIDs =
                    unchokedDownloadRateContainers.stream()
                            .map(downloadRateContainer -> downloadRateContainer.getNeighbor().getPeerID())
                            .collect(Collectors.toSet());
            System.out.println("Unchoking peer ID" + unchokedPeerIDs);
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

        private void sendChokeMessage(final boolean isChoked, final Neighbor neighbor) {
            final Message request =
                    messageFactory.createMessage(isChoked ? MessageType.CHOKE : MessageType.UNCHOKE);
            neighbor.sendMessageInOutputStream(request);
        }
    }
}
