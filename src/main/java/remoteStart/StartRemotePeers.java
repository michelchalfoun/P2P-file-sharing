package remoteStart;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class StartRemotePeers {

    private static final String scriptPrefix = "cd ~/project/ && java -jar peerProcess.jar ";

    public static class PeerInfo {

        private String peerID;
        private String hostName;

        public PeerInfo(String peerID, String hostName) {
            super();
            this.peerID = peerID;
            this.hostName = hostName;
        }

        public String getPeerID() {
            return peerID;
        }

        public void setPeerID(String peerID) {
            this.peerID = peerID;
        }

        public String getHostName() {
            return hostName;
        }

        public void setHostName(String hostName) {
            this.hostName = hostName;
        }
    }

    public static void main(String[] args) {
        System.out.println("Starting remote run");
        ArrayList<PeerInfo> peerList = new ArrayList<>();

        String ciseUser = "pabloestrada"; // change with your CISE username

        /**
         * Make sure the below peer hostnames and peerIDs match those in PeerInfo.cfg in the remote
         * CISE machines. Also make sure that the peers which have the file initially have it under
         * the 'peer_[peerID]' folder.
         */
        peerList.add(new PeerInfo("1001", "lin113-16.cise.ufl.edu"));
        peerList.add(new PeerInfo("1002", "lin113-19.cise.ufl.edu"));
        peerList.add(new PeerInfo("1003", "lin113-20.cise.ufl.edu"));
        peerList.add(new PeerInfo("1004", "lin113-21.cise.ufl.edu"));
        peerList.add(new PeerInfo("1005", "lin113-22.cise.ufl.edu"));
        peerList.add(new PeerInfo("1006", "lin113-17.cise.ufl.edu"));

        System.out.println("Added peers");

        for (PeerInfo remotePeer : peerList) {
            try {
                JSch jsch = new JSch();
                /*
                 * Give the path to your private key. Make sure your public key
                 * is already within your remote CISE machine to ssh into it
                 * without a password. Or you can use the corressponding method
                 * of JSch which accepts a password.
                 */
                System.out.println("Starting peer " + remotePeer.getPeerID() + " " + remotePeer.getHostName());
                final String password = "Harvardclose321"; // add key password
                jsch.addIdentity("/Users/pabloestrada/.ssh/id_rsa", password);
                Session session = jsch.getSession(ciseUser, remotePeer.getHostName(), 22);
                Properties config = new Properties();

                config.put("StrictHostKeyChecking", "no");

                session.setConfig(config);

                System.out.println("Attempting to connect to session");
                session.connect();
                System.out.println("Connection successful");


                System.out.println(
                        "Session to peer# "
                                + remotePeer.getPeerID()
                                + " at "
                                + remotePeer.getHostName());

                Channel channel = session.openChannel("exec");
                System.out.println("remotePeerID" + remotePeer.getPeerID());
                ((ChannelExec) channel).setCommand(scriptPrefix + remotePeer.getPeerID());

                channel.setInputStream(null);
                ((ChannelExec) channel).setErrStream(System.err);

                InputStream input = channel.getInputStream();
                channel.connect();

                System.out.println(
                        "Channel Connected to peer# "
                                + remotePeer.getPeerID()
                                + " at "
                                + remotePeer.getHostName()
                                + " server with commands");

                (new Thread(() -> {

                    InputStreamReader inputReader = new InputStreamReader(input);
                    BufferedReader bufferedReader = new BufferedReader(inputReader);
                    String line = null;

                    try {

                        while ((line = bufferedReader.readLine()) != null) {
                            System.out.println(remotePeer.getPeerID() + ">:" + line);
                        }
                        bufferedReader.close();
                        inputReader.close();
                    } catch (Exception ex) {
                        System.out.println(remotePeer.getPeerID() + " Exception >:");
                        ex.printStackTrace();
                    }

                    channel.disconnect();
                    session.disconnect();
                }))
                        .start();

            } catch (JSchException e) {
                // TODO Auto-generated catch block
                System.out.println(remotePeer.getPeerID() + " JSchException >:");
                e.printStackTrace();
            } catch (IOException ex) {
                System.out.println(remotePeer.getPeerID() + " Exception >:");
                ex.printStackTrace();
            }
        }
    }
}
