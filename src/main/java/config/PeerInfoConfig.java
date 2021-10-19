package config;

import peer.PeerMetadata;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

public class PeerInfoConfig {
    private Map<Integer, PeerMetadata> peerMetadataById = new HashMap<>();

    public PeerInfoConfig() {
        try {
            final File myObj = new File(System.getProperty("user.dir") + "/PeerInfo.cfg");
            final Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                final String data = myReader.nextLine();
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
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
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
