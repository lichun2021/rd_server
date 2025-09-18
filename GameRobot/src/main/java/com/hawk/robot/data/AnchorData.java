package com.hawk.robot.data;

import com.hawk.game.protocol.Anchor.AnchorServerInfoGS;
import com.hawk.game.protocol.Anchor.RoomInfo;

/**
 * 
 * @author zhenyu.shang
 * @since 2018年4月4日
 */
public class AnchorData {
	
	private GameRobotData robotData;
	
	private AnchorServerInfoGS serverInfo;
	
	private RoomInfo roomInfo;
	
	private AnchorState state;
	
	public AnchorData(GameRobotData robotData) {
		this.robotData = robotData;
		this.state = AnchorState.OFFLINE;
	}

	public AnchorServerInfoGS getServerInfo() {
		return serverInfo;
	}

	public void setServerInfo(AnchorServerInfoGS serverInfo) {
		this.serverInfo = serverInfo;
	}

	public GameRobotData getRobotData() {
		return robotData;
	}
	
	public AnchorState getState() {
		return state;
	}

	public void setState(AnchorState state) {
		this.state = state;
	}

	public RoomInfo getRoomInfo() {
		return roomInfo;
	}

	public void setRoomInfo(RoomInfo roomInfo) {
		this.roomInfo = roomInfo;
	}

	public void clearOffline(){
		this.roomInfo = null;
		this.serverInfo = null;
		this.state = AnchorState.OFFLINE;
	}

	public enum AnchorState{
		GETINGSERVER, CONNECT, LOGINING, ONLINE, OFFLINE;
	}
	
}
