package com.uga.participant;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageReceiverThread implements Runnable {

	@Override
	public void run() {
		
		//This thread will do following activities in each while iteraion-
		/*
		 * 1) It will open the server socket on the threadBSocketPort mentioned in ParticipantApp.java
		 * 2) It will listen for the socket connection
		 * 3) Once received connection it will read the messages using AppUtil.getServerResponse() utility (Modified One to write in file instead of console)
		 * 4) It will close the socket connection and go into the second iteration
		 *
		 */
		Thread.currentThread().setName("Message-Receiver-Thread");
		while (true) {
			try (ServerSocket listener = new ServerSocket(ParticipantApp.threadBSocketPort)) {
				
				/*
				 * Listen for a socket connection from either the MessengerThread(for single
				 * realtime multicast msg) of coordinator or the ParticipantHandlerThread (for
				 * old multicast msgs) of the coordinator
				 */
				Socket messagingSocket = listener.accept();
				Scanner messagingSocketInSc = new Scanner(messagingSocket.getInputStream());
				
				
				AppUtil.getPrintWriterResponseInFile("", messagingSocketInSc);
				
				
				messagingSocketInSc.close();
				messagingSocket.close();
	
			} catch (IOException e) {		
				e.printStackTrace();
			}
	
		}
	} //END OF THE THREAD JOB
}
