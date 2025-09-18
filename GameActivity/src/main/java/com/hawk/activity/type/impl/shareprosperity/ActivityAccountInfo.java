package com.hawk.activity.type.impl.shareprosperity;

public class ActivityAccountInfo {

	private String playerId;
	private String serverId;
	private long time;
	
	public ActivityAccountInfo() {
	}
	
	public ActivityAccountInfo(String playerId, String serverId, long time) {
		this.playerId = playerId;
		this.serverId = serverId;
		this.time = time;
	}
	
	public String getPlayerId() {
		return playerId;
	}
	
	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}
	
	public String getServerId() {
		return serverId;
	}
	
	public void setServerId(String serverId) {
		this.serverId = serverId;
	}
	
	public long getTime() {
		return time;
	}
	
	public void setTime(long time) {
		this.time = time;
	}
	
}
