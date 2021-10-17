package config;

import peer.PeerMetadata;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

public class PeerInfoConfig {

    private static final String CONFIG_FOLDER = "project_config_file_large/";
    private Map<Integer, PeerMetadata> peerMetadataById = new HashMap<>();

    public PeerInfoConfig() {
        try {
            final File myObj = new File("/Users/pabloestrada/Desktop/P2P-file-sharing/project_config_file_small/PeerInfo.cfg");
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

    public Map<Integer, PeerMetadata> getPrevPeers(String peerID) {
        return peerMetadataById.entrySet().stream()
                .filter(
                        currentPeerID ->
                                currentPeerID.getKey() < Integer.parseInt(peerID))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
