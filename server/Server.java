package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int sPort = 8000;

    public static void main(String[] args) throws Exception {
        System.out.println("The server is running.");
        ServerSocket listener = new ServerSocket(sPort);
        int clientNum = 1;
        try {
            while(true) {
                Socket socket = listener.accept();
                new Server.Handler(socket,clientNum).start();
                System.out.println("Client "  + clientNum + " is connected of " + socket.getInetAddress() + " and " + socket.getRemoteSocketAddress() + " and " + socket.getLocalSocketAddress());
                clientNum++;
            }
        } finally {
            listener.close();
        }
    }

    private static class Handler extends Thread {
        private String message;    //message received from the src.main.java.client
        private String MESSAGE;    //uppercase message send to the src.main.java.client
        private Socket connection;
        private ObjectInputStream in;	//stream read from the socket
        private ObjectOutputStream out;    //stream write to the socket
        private int no;		//The index number of the src.main.java.client

        public Handler(Socket connection, int no) {
            this.connection = connection;
            this.no = no;
        }

        public void run() {
            try{
                //initialize Input and Output streams
                out = new ObjectOutputStream(connection.getOutputStream());
                out.flush();
                in = new ObjectInputStream(connection.getInputStream());
                try{
                    while(true) {
                        //receive the message sent from the src.main.java.client
                        System.out.println(in.readObject());
                        message = (String)in.readObject();
                        //show the message to the user
                        System.out.println("Receive message: " + message + " from src.main.java.client " + no);
                        //Capitalize all letters in the message
                        MESSAGE = message.toUpperCase();
                        //send MESSAGE back to the src.main.java.client
                        sendMessage(MESSAGE);
                    }
                }
                catch(ClassNotFoundException classnot){
                    System.err.println("Data received in unknown format");
                }
            }
            catch(IOException ioException){
                System.out.println("Disconnect with Client " + no);
            }
            finally{
                //Close connections
                try{
                    in.close();
                    out.close();
                    connection.close();
                }
                catch(IOException ioException){
                    System.out.println("Disconnect with Client " + no);
                }
            }
        }

        //send a message to the output stream
        public void sendMessage(String msg){
            try{
                out.writeObject(msg);
                out.flush();
                System.out.println("Send message: " + msg + " to Client " + no);
            }
            catch(IOException ioException){
                ioException.printStackTrace();
            }
        }


    }
}
