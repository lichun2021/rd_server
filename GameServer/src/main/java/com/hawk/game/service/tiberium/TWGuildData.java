package com.hawk.game.service.tiberium;

import com.alibaba.fastjson.annotation.JSONField;
import com.hawk.game.config.CyborgConstCfg;
import com.hawk.game.protocol.TiberiumWar.TWGuildInfo;

/**
 * @author admin
 *
 */
public class TWGuildData implements Comparable<TWGuildData> {
	public String id;

	public String serverId;

	public String name;

	public String tag;

	public int flag;

	public String roomId;

	public int memberCnt;

	public long guildStrength;
	
	public long totalPower;
	
	public int eloScore;

	public long score;

	public int eloChange;

	/** 报名场次角标*/
	public int timeIndex;

	/** 是否匹配失败*/
	public boolean matchFailed;

	/** 对手联盟*/
	public String oppGuildId;
	
	/** 是否胜利*/
	public boolean isWin;
	
	/** 是否已发奖*/
	public boolean isAwarded;
	
	/** 是否正常完成结算*/
	public boolean isComplete;
	
	public long guildPower; 

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public int getMemberCnt() {
		return memberCnt;
	}

	public void setMemberCnt(int memberCnt) {
		this.memberCnt = memberCnt;
	}

	public long getTotalPower() {
		return totalPower;
	}
	

	public void setTotalPower(long totalPower) {
		this.totalPower = totalPower;
	}

	public long getGuildStrength() {
		return guildStrength;
	}

	public void setGuildStrength(long guildStrength) {
		this.guildStrength = guildStrength;
	}
	
	public long getScore() {
		return score;
	}

	public void setScore(long score) {
		this.score = score;
	}

	public int getTimeIndex() {
		return timeIndex;
	}

	public void setTimeIndex(int timeIndex) {
		this.timeIndex = timeIndex;
	}

	public boolean isMatchFailed() {
		return matchFailed;
	}

	public void setMatchFailed(boolean matchFailed) {
		this.matchFailed = matchFailed;
	}

	public String getOppGuildId() {
		return oppGuildId;
	}

	public void setOppGuildId(String oppGuildId) {
		this.oppGuildId = oppGuildId;
	}
	
	public boolean isWin() {
		return isWin;
	}

	public void setWin(boolean isWin) {
		this.isWin = isWin;
	}

	public boolean isAwarded() {
		return isAwarded;
	}

	public void setAwarded(boolean isAwarded) {
		this.isAwarded = isAwarded;
	}

	public boolean isComplete() {
		return isComplete;
	}

	public void setComplete(boolean isComplete) {
		this.isComplete = isComplete;
	}
	
	public int getEloScore() {
		return eloScore;
	}

	public void setEloScore(int eloScore) {
		this.eloScore = eloScore;
	}
	
	public long getGuildPower() {
		return guildPower;
	}

	public void setGuildPower(long guildPower) {
		this.guildPower = guildPower;
	}

	public int getEloChange() {
		return eloChange;
	}

	public void setEloChange(int eloChange) {
		this.eloChange = eloChange;
	}

	@Override
	public int compareTo(TWGuildData arg0) {
		long gap = this.eloScore - arg0.eloScore;
		if(CyborgConstCfg.getInstance().getCyborgMatchSwitch() > 0){
			gap = this.guildStrength - arg0.guildStrength;
		}
		if (gap > 0) {
			return -1;
		} else if (gap < 0) {
			return 1;
		}
		gap = this.getTotalPower() - arg0.getTotalPower();
		if (gap > 0) {
			return -1;
		} else if (gap < 0) {
			return 1;
		}
		gap = this.memberCnt - arg0.memberCnt;
		if (gap > 0) {
			return -1;
		} else if (gap < 0) {
			return 1;
		}
		return this.id.compareTo(arg0.id);
	}
	
	@JSONField(serialize = false)
	public TWGuildInfo.Builder build() {
		TWGuildInfo.Builder builder = TWGuildInfo.newBuilder();
		builder.setId(this.id);
		builder.setName(this.name);
		builder.setTag(this.tag);
		builder.setGuildFlag(flag);
		builder.setServerId(this.serverId);
		builder.setBattlePoint(this.totalPower);
		builder.setMemberCnt(this.memberCnt);
		builder.setScore(this.score);
		builder.setIsWin(this.isWin);
		return builder;
	}
}
