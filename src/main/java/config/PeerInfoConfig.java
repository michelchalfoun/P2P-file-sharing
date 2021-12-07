package config;

import peer.PeerMetadata;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

/** Utility class to read and store values from PeerInfo.cfg */
public class PeerInfoConfig {
    private final Map<Integer, PeerMetadata> peerMetadataById;

    public PeerInfoConfig() {
        peerMetadataById = new HashMap<>();
        try {
            final File peerInfoFile = new File(System.getProperty("user.dir") + "/PeerInfo.cfg");
            final Scanner scanner = new Scanner(peerInfoFile);
            while (scanner.hasNextLine()) {
                final String data = scanner.nextLine();
                final String[] parsedData = data.split(" ");
                final int peerID = Integer.parseInt(parsedData[0]);
                peerMetadataById.put(
                        peerID,
                        new PeerMetadata(
                                peerID,
                                parsedData[1],
                                Integer.parseInt(parsedData[2]),
                                parsedData[3].equals("1")));
            }
            scanner.close();
        } catch (final FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public int getNumberOfNeighbors() {
        return peerMetadataById.size() - 1;
    }

    public PeerMetadata getPeerInfo(final int peerID) {
        return peerMetadataById.get(peerID);
    }

    public Map<Integer, PeerMetadata> getPrevPeers(final Integer peerID) {
        return peerMetadataById.entrySet().stream()
                .filter(currentPeerID -> currentPeerID.getKey() < peerID)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
