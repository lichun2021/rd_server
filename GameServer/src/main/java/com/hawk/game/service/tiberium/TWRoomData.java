package com.hawk.game.service.tiberium;

import org.hawk.os.HawkTime;

import com.alibaba.fastjson.annotation.JSONField;
import com.hawk.game.service.tiberium.TiberiumConst.RoomState;
import com.hawk.game.service.tiberium.TiberiumConst.TLWBattleType;
import com.hawk.game.service.tiberium.TiberiumConst.TLWGroupType;

/**
 *  泰伯利亚之战房间匹配信息
 * @author Jesse
 */
public class TWRoomData {
	public String id;

	public String guildA;
	
	public long scoreA;

	public String guildB;
	
	public long scoreB;

	public String roomServerId;

	public int timeIndex;
	
	public RoomState roomState = RoomState.NOT_INIT;
	
	public String winnerId;
	
	public TLWGroupType group;
	
	public TLWBattleType battleType;
	
	public int teamId;
	
	/**
	 * 最后的更新时间.
	 */
	private long lastActiveTime;

	private int serverType;

	public final String getId() {
		return id;
	}

	public String getGuildA() {
		return guildA;
	}

	public void setGuildA(String guildA) {
		this.guildA = guildA;
	}

	public long getScoreA() {
		return scoreA;
	}

	public void setScoreA(long scoreA) {
		this.scoreA = scoreA;
	}

	public String getGuildB() {
		return guildB;
	}

	public void setGuildB(String guildB) {
		this.guildB = guildB;
	}

	public long getScoreB() {
		return scoreB;
	}

	public void setScoreB(long scoreB) {
		this.scoreB = scoreB;
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
	
	public String getWinnerId() {
		return winnerId;
	}

	public void setWinnerId(String winnerId) {
		this.winnerId = winnerId;
	}

	public TLWGroupType getGroup() {
		return group;
	}

	public void setGroup(TLWGroupType group) {
		this.group = group;
	}
	
	public TLWBattleType getBattleType() {
		return battleType;
	}
	
	public void setBattleType(TLWBattleType battleType) {
		this.battleType = battleType;
	}

	public int getTeamId() {
		return teamId;
	}

	public void setTeamId(int teamId) {
		this.teamId = teamId;
	}

	@JSONField(serialize = false)
	public String getOppGuildId(String guildId) {
		if (guildId.equals(this.guildA)) {
			return this.guildB;
		} else if (guildId.equals(this.guildB)) {
			return this.guildA;
		} else {
			return null;
		}
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

	public int getServerType() {
		return serverType;
	}

	public void setServerType(int serverType) {
		this.serverType = serverType;
	}
}
