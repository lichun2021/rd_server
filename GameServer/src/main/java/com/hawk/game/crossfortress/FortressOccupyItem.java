package com.hawk.game.crossfortress;

/**
 * 要塞占领信息item
 * @author golden
 *
 */
public class FortressOccupyItem {

	private String serverId;
	
	private String currServer;
	
	private int pointId;
	
	public FortressOccupyItem() {
		
	}
	
	public FortressOccupyItem(String serverId, String currServer, int pointId) {
		this.serverId = serverId;
		this.currServer = currServer;
		this.pointId = pointId;
	}
	
	@Override
	public String toString() {
		return serverId + ":" + currServer + ":" + pointId;
	}
	
	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getCurrServer() {
		return currServer;
	}

	public void setCurrServer(String currServer) {
		this.currServer = currServer;
	}

	public int getPointId() {
		return pointId;
	}

	public void setPointId(int pointId) {
		this.pointId = pointId;
	}
}
