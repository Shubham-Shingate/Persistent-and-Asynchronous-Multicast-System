package com.uga.coordinator;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public class MessengerThread implements Runnable {

	@Override
	public void run() {
		
		/**--------Activities for messenger thread
		 * 1) Will wait for the notifiction of a new message
		 * 2) Once notified it will inter into a for loop 
		 *        a) will open the socket connection to current online participant's messaging port (Handled by threadB of each paricipant)
		 *        b) send the message
		 *        c) close the socket connection
		 *        d) go to next iteration of loop
		 * 3) Once message is sent, it will again call wait() operation on the messageBuffer
		 */
		Thread.currentThread().setName("Messenger-Thread");
		while (true) {
			//Calls a wait method on the Cordinator.messageBuffer object.
			try {
				synchronized (Coordinator.messageBuffer) {
					Coordinator.messageBuffer.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			TreeMap<Date, String> sortedMessageBuffer = new TreeMap<Date, String>(Coordinator.messageBuffer);
			//Deliver the messages to all the online (connected) participants.
			for (Map.Entry<String, Participant> participant : Coordinator.participants.entrySet()) {
				if (participant.getValue().getStatus().equals("Online")) {
					// a) will open the socket connection to current online participant's messaging port (Handled by threadB of each paricipant)
					Socket messagingSocket = null;
					PrintWriter messagingSocketOutPw = null;
					try {
						//Open the socket to the thread-B of this particular participant.
						messagingSocket = new Socket(participant.getValue().getIpAddress(),
								participant.getValue().getMessagingPortNum());
						messagingSocketOutPw = new PrintWriter(messagingSocket.getOutputStream(), true);
						// send the message to this particular participant
						messagingSocketOutPw.println(sortedMessageBuffer.lastEntry().getKey().toString()+" --> "+sortedMessageBuffer.lastEntry().getValue()+"\n");
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						//Close the socket
						messagingSocketOutPw.close();
						try {
							messagingSocket.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

				}
			} 
		}

	}
	
	
}
