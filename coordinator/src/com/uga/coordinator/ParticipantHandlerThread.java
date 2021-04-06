package com.uga.coordinator;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.Map;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.TreeMap;

public class ParticipantHandlerThread implements Runnable {

	private Socket socket;
	private String participantId;

	public ParticipantHandlerThread(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {

		try {
			OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();
            PrintWriter socketOutPw = new PrintWriter(out,true);
            Scanner socketInSc = new Scanner(in);
            String[] commandArr;
           
			while (true) { //SAFE POINT TO WAIT
				String line = socketInSc.nextLine();
                commandArr = line.split(" ", 2);
                
                switch (commandArr[0]) {
				
                	case AppConstants.REGISTER:
					if (AppUtil.checkPattern("^register ([0-9]{4})$", line)) {
						//Read the participant ID
						String participantId = socketInSc.nextLine();
						//Read the participant IP address from socket
						String participantIPAddress = socket.getInetAddress().getHostAddress();
						//Read the participant's thread-B (messaging) Port number from socket
						String messagingPortNum = commandArr[1];
						/**--Register the participant--*/
						if (Coordinator.participants.containsKey(participantId)) {
							socketOutPw.println("The participant ID already exists, try with a new ID\n");
						} else {

							for (Map.Entry<String, Participant> participant : Coordinator.participants.entrySet()) {
								if (Thread.currentThread().getName().equals(participant.getValue().getThreadId())) {
									socketOutPw.println("The participant is already registered with a different ID\n");
									break;
								}
							}

							this.participantId = participantId;
							Coordinator.participants.put(participantId,
									new Participant(participantId, participantIPAddress,
											Integer.parseInt(messagingPortNum), "Online", "Y",
											new Date(System.currentTimeMillis()), Thread.currentThread().getName()));
							socketOutPw.println("Done\n");
						} 
					} else {
						socketOutPw.println("Invalid Command..\n");
					}
					break;
				
                	case AppConstants.DEREGISTER:
					if (AppUtil.checkPattern("^deregister$", line)) {
						//Remove the participant from the multicast group
						if (Coordinator.participants.containsKey(this.participantId)) {
							Coordinator.participants.remove(this.participantId);
							socketOutPw.println("Done\n");
						} else {
							socketOutPw.println("This participant is not yet registered, register first to deregister\n");
						} 
					} else {
						socketOutPw.println("Invalid Command..\n");
					}
					break;
				
                	case AppConstants.DISCONNECT:
					if (AppUtil.checkPattern("^disconnect$", line)) {
						//Change the status of the participant from online to offline
						if (Coordinator.participants.containsKey(this.participantId)
								&& Coordinator.participants.get(this.participantId).getStatus().equals("Online")) {
							//Make the participant offline
							Coordinator.participants.get(this.participantId).setStatus("Offline");
							Coordinator.participants.get(this.participantId)
									.setLastUpdated(new Date(System.currentTimeMillis()));
							socketOutPw.println("Done\n");
						} else if (!Coordinator.participants.containsKey(this.participantId)) {
							socketOutPw.println("This participant is not registered\n");
						} else {
							socketOutPw.println("This participant is already disconnected\n");
						} 
					} else {
						socketOutPw.println("Invalid Command..\n");
					}
					break;	
					
                	case AppConstants.RECONNECT:
                	
					if (AppUtil.checkPattern("^reconnect ([0-9]{4})$", line)) {
						if (Coordinator.participants.containsKey(this.participantId)
								&& Coordinator.participants.get(this.participantId).getStatus().equals("Offline")) {
							Socket messagingSocket = null;
							PrintWriter messagingSocketOutPw = null;
							try {
								//Open the socket to the thread-B of this particular participant where this instance of co-ordinator thread is connected.
								messagingSocket = new Socket(socket.getInetAddress().getHostAddress(),
										Coordinator.participants.get(this.participantId).getMessagingPortNum());
								messagingSocketOutPw = new PrintWriter(messagingSocket.getOutputStream(), true);

								//Then send all the messages to the participant that where posted in the message queue after td (Threshold Time)
								TreeMap<Date, String> sortedMessageBuffer = new TreeMap<Date, String>(
										Coordinator.messageBuffer);

								SortedMap<Date, String> messagesAfterTd = sortedMessageBuffer.subMap(
										new Date(System.currentTimeMillis() - Coordinator.thresholdTd * 1000),
										new Date(System.currentTimeMillis()));
								if (messagesAfterTd.size() == 0) {
									messagingSocketOutPw.println("");
								}
								int iterationCount = 0;
								//Send all the messages over the participants messaging socket
								for (Map.Entry<Date, String> message : messagesAfterTd.entrySet()) {
									iterationCount++;
									if (iterationCount == messagesAfterTd.entrySet().size()) {
										messagingSocketOutPw.println(
												message.getKey().toString() + " --> " + message.getValue() + "\n");
									} else {
										messagingSocketOutPw
												.println(message.getKey().toString() + " --> " + message.getValue());
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
							} finally {
								//Close the socket connection to the participant
								messagingSocketOutPw.close();
								messagingSocket.close();
							}
							//Finally change the status and update the message port of the participant in the participant table.
							Coordinator.participants.get(this.participantId).setStatus("Online");
							Coordinator.participants.get(this.participantId)
									.setLastUpdated(new Date(System.currentTimeMillis()));
							Coordinator.participants.get(this.participantId)
									.setMessagingPortNum(Integer.parseInt(commandArr[1]));

							socketOutPw.println("Done\n");
						} else if (!Coordinator.participants.containsKey(this.participantId)) {
							socketOutPw.println("This participant is not registered\n");
						} else {
							socketOutPw.println("This participant is already connected (online)\n");
						} 
					} else {
						socketOutPw.println("Invalid Command..\n");
					}
					break;
                		
                	case AppConstants.MSEND:
					if (commandArr.length >= 2 && commandArr[0].equals("msend") && AppUtil.checkPattern("^[^\\s].*$", commandArr[1])) {
						//Put the message into the message buffer (queue) only if participant is registered and online.
						String multiCastMsg = commandArr[1];
						if (Coordinator.participants.containsKey(this.participantId)
								&& Coordinator.participants.get(this.participantId).getStatus().equals("Online")) {
							synchronized (Coordinator.messageBuffer) {
								Coordinator.messageBuffer.put(new Date(System.currentTimeMillis()), "(ParticipantID-"+this.participantId+"): "+multiCastMsg);
								/**---- Do a notifyAll() call on the Coordinator.messagebuffer object to notify Messenger thread to deliver a new message to all online participants -----*/
								Coordinator.messageBuffer.notifyAll();
							}
							socketOutPw.println("Done\n");
						} else if (!Coordinator.participants.containsKey(this.participantId)) {
							socketOutPw.println("This participant is not registered\n");
						} else {
							socketOutPw.println("This participant is not connected (offline)\n");
						} 
					} else {
						socketOutPw.println("Invalid Command..\n");
					}
					break;
                		
                	default:
                		socketOutPw.println("Invalid Command..\n");
                		break;
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	} //END OF THE THREAD JOB

}
