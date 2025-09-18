package com.hawk.game.service.cyborgWar;

/**
 * 战队持久化数据
 * 
 * @author Jesse
 *
 */
public class CWTeamData {
	public String id;

	public String guildId;

	public String name;

	public String guildTag;

	public String guildName;

	public int guildFlag;

	public String serverId;

	public long createTime;

	public int termId;

	public int timeIndex;

	/** 段位 */
	public int star;

	/** 段位初始化赛季 */
	public int initSeason;

	/** 段位初始化排名百分比 */
	public int initPercent;

	/** 段位初始化额外加星 */
	public int initExtStar;
	
	/** 赛季首期初始化排名*/
	public int initRank;

	/** 赛季总积分 */
	public long seasonScore;

	/** 段位结算奖励赛季 */
	public int rewardedSeason;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGuildTag() {
		return guildTag;
	}

	public void setGuildTag(String guildTag) {
		this.guildTag = guildTag;
	}

	public String getGuildName() {
		return guildName;
	}

	public void setGuildName(String guildName) {
		this.guildName = guildName;
	}

	public int getGuildFlag() {
		return guildFlag;
	}

	public void setGuildFlag(int guildFlag) {
		this.guildFlag = guildFlag;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public int getTimeIndex() {
		return timeIndex;
	}

	public void setTimeIndex(int timeIndex) {
		this.timeIndex = timeIndex;
	}

	public int getStar() {
		return star;
	}

	public void setStar(int star) {
		this.star = star;
	}

	public int getInitSeason() {
		return initSeason;
	}

	public void setInitSeason(int initSeason) {
		this.initSeason = initSeason;
	}

	public int getInitPercent() {
		return initPercent;
	}

	public void setInitPercent(int initPercent) {
		this.initPercent = initPercent;
	}

	public int getInitExtStar() {
		return initExtStar;
	}

	public void setInitExtStar(int initExtStar) {
		this.initExtStar = initExtStar;
	}

	public int getInitRank() {
		return initRank;
	}

	public void setInitRank(int initRank) {
		this.initRank = initRank;
	}

	public long getSeasonScore() {
		return seasonScore;
	}

	public void setSeasonScore(long seasonScore) {
		this.seasonScore = seasonScore;
	}

	public int getRewardedSeason() {
		return rewardedSeason;
	}

	public void setRewardedSeason(int rewardedSeason) {
		this.rewardedSeason = rewardedSeason;
	}

}
