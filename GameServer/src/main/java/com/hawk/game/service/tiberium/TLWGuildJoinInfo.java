package com.hawk.game.service.tiberium;

import com.hawk.game.service.tiberium.TiberiumConst.TLWGroupType;
import com.hawk.game.service.tiberium.TiberiumConst.TLWEliminationGroupType;
/**
 * 联赛联盟战斗数据
 * 
 * @author admin
 *
 */
public class TLWGuildJoinInfo{

	public String id;

	public int teamId;

	public String serverId;

	public TLWGroupType group;
	
	public TLWGroupType initGroup;
	
	public TLWEliminationGroupType eliminationGroup;

	/** 是否种子选手 */
	public boolean isSeed;

	public long score;

	public int winCnt;

	public int loseCnt;

	/**
	 * 上次匹配出战的战力
	 */
	public long lastestPower;

	/**
	 * 入围时战力
	 */
	public long initPower;
	
	/**
	 * 入围时联盟战力排行
	 */
	public int initPowerRank;
	
	/**
	 * 循环赛结束时小组排名
	 */
	public int teamRank;

	/**
	 * 同名次组内排名
	 */
	public int positionGroupRank;
	
	
	/**
	 * 被淘汰的轮次
	 */
	public int kickOutTerm = 100;
	
	/**
	 * 联盟创建时间
	 */
	public long createTime;

	public int serverType;

	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getTeamId() {
		return teamId;
	}

	public void setTeamId(int teamId) {
		this.teamId = teamId;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public TLWGroupType getGroup() {
		return group;
	}

	public void setGroup(TLWGroupType group) {
		this.group = group;
	}

	public TLWGroupType getInitGroup() {
		return initGroup;
	}

	public void setInitGroup(TLWGroupType initGroup) {
		this.initGroup = initGroup;
	}

	public TLWEliminationGroupType getEliminationGroup() {
		return eliminationGroup;
	}
	
	public void setEliminationGroup(TLWEliminationGroupType eliminationGroup) {
		this.eliminationGroup = eliminationGroup;
	}
	
	public boolean isSeed() {
		return isSeed;
	}

	public void setSeed(boolean isSeed) {
		this.isSeed = isSeed;
	}

	public long getScore() {
		return score;
	}

	public void setScore(long score) {
		this.score = score;
	}

	public int getWinCnt() {
		return winCnt;
	}

	public void setWinCnt(int winCnt) {
		this.winCnt = winCnt;
	}

	public int getLoseCnt() {
		return loseCnt;
	}

	public void setLoseCnt(int loseCnt) {
		this.loseCnt = loseCnt;
	}

	public long getLastestPower() {
		return lastestPower;
	}

	public void setLastestPower(long lastestPower) {
		this.lastestPower = lastestPower;
	}

	public int getKickOutTerm() {
		return kickOutTerm;
	}

	public void setKickOutTerm(int kickOutTerm) {
		this.kickOutTerm = kickOutTerm;
	}

	public long getInitPower() {
		return initPower;
	}

	public void setInitPower(long initPower) {
		this.initPower = initPower;
	}

	public int getInitPowerRank() {
		return initPowerRank;
	}

	public void setInitPowerRank(int initPowerRank) {
		this.initPowerRank = initPowerRank;
	}

	public int getTeamRank() {
		return teamRank;
	}

	public void setTeamRank(int teamRank) {
		this.teamRank = teamRank;
	}
	
	public void setPositionGroupRank(int positionGroupRank) {
		this.positionGroupRank = positionGroupRank;
	}
	
	public int getPositionGroupRank() {
		return positionGroupRank;
	}
	
	
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	
	public long getCreateTime() {
		return createTime;
	}

	public int getServerType() {
		return serverType;
	}

	public void setServerType(int serverType) {
		this.serverType = serverType;
	}
}
