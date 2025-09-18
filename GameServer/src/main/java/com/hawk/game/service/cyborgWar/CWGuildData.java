package com.hawk.game.service.cyborgWar;

/**
 * 联赛联盟基础数据-定时刷新,联盟数据源
 * 
 * @author admin
 *
 */
public class CWGuildData implements Comparable<CWGuildData> {
	/** 赛季 */
	public int termId;

	public String id;

	public String serverId;

	public String name;

	public String tag;

	public int flag;

	public String leaderId;

	public String leaderName;

	public long power;

	public CWGuildData() {
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
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

	public String getLeaderId() {
		return leaderId;
	}

	public void setLeaderId(String leaderId) {
		this.leaderId = leaderId;
	}

	public String getLeaderName() {
		return leaderName;
	}

	public void setLeaderName(String leaderName) {
		this.leaderName = leaderName;
	}

	public long getPower() {
		return power;
	}

	public void setPower(long power) {
		this.power = power;
	}

	@Override
	public int compareTo(CWGuildData arg0) {
		long gap = this.getPower() - arg0.getPower();
		if (gap > 0) {
			return -1;
		} else if (gap < 0) {
			return 1;
		}
		return this.id.compareTo(arg0.id);
	}
}
