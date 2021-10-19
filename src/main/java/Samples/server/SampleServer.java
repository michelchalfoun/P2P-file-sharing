package Samples.server;

import messages.HandshakeMessage;

import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class SampleServer {

	private static final int sPort = 8000;   //The server will be listening on this port number

	public static void main(String[] args) throws Exception {
		System.out.println("The server is running."); 
        ServerSocket listener = new ServerSocket(sPort);
		int clientNum = 1;
        	try {
				while(true) {
					Socket socket = listener.accept();
					new SampleServer.Handler(socket,clientNum).start();
					System.out.println("Client "  + clientNum + " is connected of " + socket.getInetAddress() + " and " + socket.getRemoteSocketAddress() + " and " + socket.getLocalSocketAddress());
					clientNum++;
            	}
        	} finally {
				System.out.println("closing");
            	listener.close();
        	}
    	}

		/**
     	* A handler thread class.  Handlers are spawned from the listening
     	* loop and are responsible for dealing with a single src.main.java.client's requests.
     	*/
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
				System.out.println("RUN1");
				try{
					while(true){
						System.out.println("WHY???");
						HandshakeMessage obj = (HandshakeMessage) in.readObject();
						System.out.println(obj);
						System.out.println("RUNNING THIS");
//						System.out.println((HandshakeMessage) obj);
						String message = "(String) obj";
						//show the message to the user
						System.out.println("Receive message: " + message + " from src.main.java.client " + no);
						//Capitalize all letters in the message
						MESSAGE = message.toUpperCase();
						//send MESSAGE back to the src.main.java.client
						sendMessage(MESSAGE);
					}
				}
				catch(ClassNotFoundException classnot){
					System.out.println(classnot);
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
