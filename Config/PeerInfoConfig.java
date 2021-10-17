package Config;

import client.Client;

import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.*;
import java.util.stream.Collectors;

public class PeerInfoConfig {

    private final static String CONFIG_FOLDER = "project_config_file_large/";
    private Map<String, List<String>> peers = new HashMap<>();
    public static<T> T[] subArray(T[] array, int beg, int end) {
        return Arrays.copyOfRange(array, beg, end + 1);
    }

    public PeerInfoConfig() {
        try {
            File myObj = new File(new File(".").getParentFile(),CONFIG_FOLDER + "PeerInfo.cfg");;
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String[] parsedPeer = data.split(" ");
                List<String> parsedData = Arrays.asList(subArray(parsedPeer, 1, parsedPeer.length));
                peers.put(parsedPeer[0], parsedData);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public List<String> getPeerInfo(String peerID){
        return peers.get(peerID);
    }

    public Map<String, List<String>> getPrevPeers(String peerID) {
        return peers.entrySet()
                .stream()
                .filter(currentPeerID -> Integer.parseInt(currentPeerID.getKey()) < Integer.parseInt(peerID))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
    }

    public Map<String, List<String>> getAllPeers(){
        return peers;
    }

}
