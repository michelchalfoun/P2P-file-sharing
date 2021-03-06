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

public class ResetRemotePeers {

    private static final String scriptPrefix = "ps -efw | grep java | grep -v grep | awk '{print $2}' | xargs kill -9";

    public static void main(String[] args) {
        System.out.println("Starting remote run");
        ArrayList<PeerInfo> peerList = RemotePeersData.peerList;

        System.out.println("Added peers");

        for (PeerInfo remotePeer : peerList) {
            try {
                JSch jsch = new JSch();

                System.out.println("Starting peer " + remotePeer.getPeerID() + " " + remotePeer.getHostName());
                jsch.addIdentity(RemotePeersData.keyLocation, RemotePeersData.keyPassword);
                Session session = jsch.getSession(RemotePeersData.ciseUsername, remotePeer.getHostName(), 22);
                Properties config = new Properties();

                config.put("StrictHostKeyChecking", "no");
                session.setConfig(config);
                session.connect();
                System.out.println("Connection successful");

                System.out.println(
                        "Session to peer# "
                                + remotePeer.getPeerID()
                                + " at "
                                + remotePeer.getHostName());

                Channel channel = session.openChannel("exec");
                System.out.println("remotePeerID " + remotePeer.getPeerID());
                ((ChannelExec) channel).setCommand(scriptPrefix);

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
