package remoteStart;

import java.util.ArrayList;

public class RemotePeersData {
    public static ArrayList<PeerInfo> peerList = new ArrayList<>();
    public static String keyLocation = "/Users/masenbeliveau/.ssh/id_rsa";
    public static String keyPassword = "password";
    public static String ciseUsername = "mbeliveau";

//    public static final String scriptPrefix = "cd ~/project_config_file_large/ && java -jar peerProcess.jar ";

    public static final String scriptPrefix = "cd ~/project_config_file_small/ && java -jar peerProcess.jar ";


    /**
     * Make sure the below peer hostnames and peerIDs match those in PeerInfo.cfg in the remote
     * CISE machines. Also make sure that the peers which have the file initially have it under
     * the 'peer_[peerID]' folder.
     */
    static {
//        peerList.add(new PeerInfo("1001", "lin113-16.cise.ufl.edu"));
//        peerList.add(new PeerInfo("1002", "lin113-19.cise.ufl.edu"));
//        peerList.add(new PeerInfo("1003", "lin113-20.cise.ufl.edu"));
//        peerList.add(new PeerInfo("1004", "lin113-21.cise.ufl.edu"));
//        peerList.add(new PeerInfo("1005", "lin113-22.cise.ufl.edu"));
//        peerList.add(new PeerInfo("1006", "lin113-17.cise.ufl.edu"));

        peerList.add(new PeerInfo("1001", "lin113-14.cise.ufl.edu"));
        peerList.add(new PeerInfo("1002", "lin113-15.cise.ufl.edu"));
        peerList.add(new PeerInfo("1003", "lin113-16.cise.ufl.edu"));
        peerList.add(new PeerInfo("1004", "lin113-17.cise.ufl.edu"));
        peerList.add(new PeerInfo("1005", "lin113-18.cise.ufl.edu"));
        peerList.add(new PeerInfo("1006", "lin113-19.cise.ufl.edu"));
        peerList.add(new PeerInfo("1007", "lin113-20.cise.ufl.edu"));
        peerList.add(new PeerInfo("1008", "lin113-21.cise.ufl.edu"));
        peerList.add(new PeerInfo("1009", "lin113-22.cise.ufl.edu"));
    }
}
