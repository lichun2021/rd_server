package com.hawk.game.service.starwars;

import java.util.List;

import org.hawk.os.HawkTime;

import com.hawk.game.service.starwars.StarWarsConst.SWRoomState;

/**
 *  星球大战房间匹配信息
 * @author Jesse
 */
public class SWRoomData {
	public String id;

	public List<String> guildList;

	public String roomServerId;

	public SWRoomState roomState = SWRoomState.NOT_INIT;

	public String winnerId;

	/** 大区id(第一第二场用)*/
	public int zoneId;

	/** 小组id(仅第一场用)*/
	public int teamId;

	/**
	 * 最后的更新时间.
	 */
	private long lastActiveTime;

	private String bilingInforStr;

	public final String getId() {
		return id;
	}

	public List<String> getGuildList() {
		return guildList;
	}

	public void setGuildList(List<String> guildList) {
		this.guildList = guildList;
	}

	public String getRoomServerId() {
		return roomServerId;
	}

	public void setRoomServerId(String roomServerId) {
		this.roomServerId = roomServerId;
	}

	public void setId(String id) {
		this.id = id;
	}

	public SWRoomState getRoomState() {
		return roomState;
	}

	public void setRoomState(SWRoomState roomState) {
		this.roomState = roomState;
	}

	public String getWinnerId() {
		return winnerId;
	}

	public void setWinnerId(String winnerId) {
		this.winnerId = winnerId;
	}

	public int getZoneId() {
		return zoneId;
	}

	public void setZoneId(int zoneId) {
		this.zoneId = zoneId;
	}

	public int getTeamId() {
		return teamId;
	}

	public void setTeamId(int teamId) {
		this.teamId = teamId;
	}

	public boolean isActive(long periodTime) {
		long curTime = HawkTime.getMillisecond();
		if (curTime - lastActiveTime < 10 * 1000 + periodTime) {
			return true;
		} else {
			return false;
		}
	}

	public long getLastActiveTime() {
		return lastActiveTime;
	}

	public void setLastActiveTime(long lastActiveTime) {
		this.lastActiveTime = lastActiveTime;
	}

	public String getBilingInforStr() {
		return bilingInforStr;
	}

	public void setBilingInforStr(String bilingInforStr) {
		this.bilingInforStr = bilingInforStr;
	}

}
