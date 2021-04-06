package com.uga.coordinator;

import java.util.Date;

public class Participant {
    
	private String participantId;
	
	private String ipAddress;
	
	private int messagingPortNum;
	
	private String status;
	
	private String registered;
	
	private Date lastUpdated;
	
	private String threadId;

	public String getParticipantId() {
		return participantId;
	}

	public void setParticipantId(String participantId) {
		this.participantId = participantId;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public int getMessagingPortNum() {
		return messagingPortNum;
	}

	public void setMessagingPortNum(int messagingPortNum) {
		this.messagingPortNum = messagingPortNum;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRegistered() {
		return registered;
	}

	public void setRegistered(String registered) {
		this.registered = registered;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public String getThreadId() {
		return threadId;
	}

	public void setThreadId(String threadId) {
		this.threadId = threadId;
	}
	
	public Participant(String participantId, String ipAddress, int messagingPortNum, String status, String registered,
			Date lastUpdated, String threadId) {
		this.participantId = participantId;
		this.ipAddress = ipAddress;
		this.messagingPortNum = messagingPortNum;
		this.status = status;
		this.registered = registered;
		this.lastUpdated = lastUpdated;
		this.threadId = threadId;
	}

	public Participant() {
		
	}

	@Override
	public String toString() {
		return "Participant [participantId=" + participantId + ", ipAddress=" + ipAddress + ", messagingPortNum="
				+ messagingPortNum + ", status=" + status + ", registered=" + registered + ", lastUpdated="
				+ lastUpdated + ", threadId=" + threadId + "]";
	}
	
	
}
