package com.hawk.game.service.cyborgWar;

import java.util.Map;

import org.hawk.os.HawkTime;

import com.hawk.game.service.cyborgWar.CWConst.RoomState;


/**
 *  赛博之战房间匹配信息
 * @author Jesse
 */
public class CWRoomData {
	public String id;

	/** 联盟id-战队id */
	public Map<String, String> gtMaps;

	/** 战队id_积分 */
	public Map<String, Long> socreMap;

	/** 战队id,排行 */
	public Map<String, Integer> rankMap;

	public String roomServerId;

	public int timeIndex;

	public RoomState roomState = RoomState.NOT_INIT;

	/**
	 * 最后的更新时间.
	 */
	private long lastActiveTime;

	public final String getId() {
		return id;
	}

	public Map<String, String> getGtMaps() {
		return gtMaps;
	}

	public void setGtMaps(Map<String, String> gtMaps) {
		this.gtMaps = gtMaps;
	}

	public Map<String, Long> getSocreMap() {
		return socreMap;
	}

	public void setSocreMap(Map<String, Long> socreMap) {
		this.socreMap = socreMap;
	}

	public Map<String, Integer> getRankMap() {
		return rankMap;
	}

	public void setRankMap(Map<String, Integer> rankMap) {
		this.rankMap = rankMap;
	}

	public String getRoomServerId() {
		return roomServerId;
	}

	public void setRoomServerId(String roomServerId) {
		this.roomServerId = roomServerId;
	}

	public int getTimeIndex() {
		return timeIndex;
	}

	public void setTimeIndex(int timeIndex) {
		this.timeIndex = timeIndex;
	}

	public void setId(String id) {
		this.id = id;
	}

	public RoomState getRoomState() {
		return roomState;
	}

	public void setRoomState(RoomState roomState) {
		this.roomState = roomState;
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
}
