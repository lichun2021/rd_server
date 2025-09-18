package com.hawk.game.service.tiberium;

import com.alibaba.fastjson.annotation.JSONField;
import com.hawk.game.protocol.TiberiumWar.TLWGuildBaseInfo;

/**
 * 联赛联盟基础数据-定时刷新,联盟数据源
 * @author admin
 *
 */
public class TLWGuildData {
	/**赛季*/
	public int season;
	
	public String id;

	public String serverId;

	public String name;

	public String tag;

	public int flag;
	
	public String leaderId;
	
	public String leadegOpenid;
	
	public String leaderName;
	
	public long power;
	
	public int joinMemberCnt;
	
	public int totalMemberCnt;

	public long createTime;

	public int serverType;
	
	public TLWGuildData() {
	}

	public int getSeason() {
		return season;
	}

	public void setSeason(int season) {
		this.season = season;
	}

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
	
	public String getLeaderName() {
		return leaderName;
	}

	public void setLeaderName(String leaderName) {
		this.leaderName = leaderName;
	}
	
	public String getLeaderId() {
		return leaderId;
	}

	public void setLeaderId(String leaderId) {
		this.leaderId = leaderId;
	}

	public String getLeadegOpenid() {
		return leadegOpenid;
	}

	public void setLeadegOpenid(String leadegOpenid) {
		this.leadegOpenid = leadegOpenid;
	}

	public long getPower() {
		return power;
	}

	public void setPower(long power) {
		this.power = power;
	}
	
	public int getJoinMemberCnt() {
		return joinMemberCnt;
	}

	public void setJoinMemberCnt(int joinMemberCnt) {
		this.joinMemberCnt = joinMemberCnt;
	}

	public int getTotalMemberCnt() {
		return totalMemberCnt;
	}

	public void setTotalMemberCnt(int totalMemberCnt) {
		this.totalMemberCnt = totalMemberCnt;
	}
	
	public long getCreateTime() {
		return createTime;
	}
	
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public int getServerType() {
		return serverType;
	}

	public void setServerType(int serverType) {
		this.serverType = serverType;
	}

	@JSONField(serialize = false)
	public TLWGuildBaseInfo.Builder genBaseInfo(){
		TLWGuildBaseInfo.Builder builder = TLWGuildBaseInfo.newBuilder();
		builder.setId(this.id);
		builder.setName(this.name);
		builder.setTag(this.tag);
		builder.setGuildFlag(this.flag);
		builder.setLeaderName(this.leaderName);
		builder.setServerId(this.serverId);
		return builder;
	}
}
