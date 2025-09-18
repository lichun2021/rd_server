package com.hawk.game.service.tiberium.logunit;

import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.player.Player;
import com.hawk.game.service.guildTeam.model.GuildTeamData;
import com.hawk.game.service.tblyTeam.model.TBLYSeasonData;
import com.hawk.game.service.tiberium.TLWGuildData;
import com.hawk.game.service.tiberium.TLWGuildJoinInfo;
import com.hawk.game.service.tiberium.TiberiumConst.TLWGroupType;

/**
 * 泰伯利亚联赛出战联盟信息元
 * 
 * @author jesse
 *
 */
public class TLWLeaguaGuildInfoUnit {
	/** 赛季 */
	private int season;

	/** 期数 */
	private int termId;

	/** 联盟Id */
	private String guildId;

	/** 联盟所在服 */
	private String guildName;

	/** 联盟名称 */
	private String guildServer;

	/** 联盟旗帜 */
	private int guildFlag;

	/** 联盟所在小组id */
	private int teamId;

	/** 联盟所在分组 */
	private TLWGroupType groupType;

	/** 是否是种子队伍 */
	private boolean isSeed;

	/** 盟主id */
	private String leaderId;

	/** 盟主名 */
	private String leaderName;

	/** 盟主openid */
	private String leaderOpenid;

	/** 联盟出战人员数量 */
	private int memberCnt;

	/** 联盟胜利次数 */
	private int winCnt;

	/** 联盟失败次数 */
	private int loseCnt;

	/** 联盟战场总积分 */
	private long totalScore;
	
	/** 淘汰期数*/
	private long kickOutTerm;

	private int serverType;

	public TLWLeaguaGuildInfoUnit(int termId, TLWGuildJoinInfo joinInfo, TLWGuildData guildData) {
		super();
		this.season = guildData.getSeason();
		this.termId = termId;
		this.guildId = guildData.getId();
		this.guildName = guildData.getName();
		this.guildFlag = guildData.getFlag();
		this.guildServer = guildData.getServerId();
		this.teamId = joinInfo.getTeamId();
		this.groupType = joinInfo.getInitGroup();
		this.isSeed = joinInfo.isSeed();
		this.leaderId = guildData.getLeaderId();
		this.leaderName = guildData.getLeaderName();
		this.leaderOpenid = guildData.getLeadegOpenid();
		this.memberCnt = guildData.getJoinMemberCnt();
		this.winCnt = joinInfo.getWinCnt();
		this.loseCnt = joinInfo.getLoseCnt();
		this.totalScore = joinInfo.getScore();
		this.kickOutTerm = joinInfo.getKickOutTerm();
		this.serverType = joinInfo.getServerType();
	}

	public TLWLeaguaGuildInfoUnit(int season, int termId, TBLYSeasonData seasonData, GuildInfoObject guildInfoObject, GuildTeamData teamData, Player leader) {
		super();
		this.season = season;
		this.termId = termId;
		this.guildId = guildInfoObject.getId();
		this.guildName = guildInfoObject.getName();
		this.guildFlag = guildInfoObject.getFlagId();
		this.guildServer = guildInfoObject.getServerId();
		this.teamId = seasonData.groupId;
		this.groupType = TLWGroupType.getType(seasonData.groupType);
		this.isSeed = seasonData.isSeed;
		this.leaderId = guildInfoObject.getLeaderId();
		this.leaderName = guildInfoObject.getLeaderName();
		this.leaderOpenid = leader!=null? leader.getOpenId():"";
		this.memberCnt = teamData.memberCnt;
		this.winCnt = seasonData.winCnt;
		this.loseCnt = seasonData.loseCnt;
		this.totalScore = seasonData.score;
		this.kickOutTerm = seasonData.kickOutTerm;
		this.serverType = seasonData.teamType;
	}

	public int getSeason() {
		return season;
	}

	public void setSeason(int season) {
		this.season = season;
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}

	public String getGuildName() {
		return guildName;
	}

	public void setGuildName(String guildName) {
		this.guildName = guildName;
	}

	public String getGuildServer() {
		return guildServer;
	}

	public void setGuildServer(String guildServer) {
		this.guildServer = guildServer;
	}

	public int getGuildFlag() {
		return guildFlag;
	}

	public void setGuildFlag(int guildFlag) {
		this.guildFlag = guildFlag;
	}

	public int getTeamId() {
		return teamId;
	}

	public void setTeamId(int teamId) {
		this.teamId = teamId;
	}

	public TLWGroupType getGroupType() {
		return groupType;
	}

	public void setGroupType(TLWGroupType groupType) {
		this.groupType = groupType;
	}

	public boolean isSeed() {
		return isSeed;
	}

	public void setSeed(boolean isSeed) {
		this.isSeed = isSeed;
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

	public String getLeaderOpenid() {
		return leaderOpenid;
	}

	public void setLeaderOpenid(String leaderOpenid) {
		this.leaderOpenid = leaderOpenid;
	}

	public int getMemberCnt() {
		return memberCnt;
	}

	public void setMemberCnt(int memberCnt) {
		this.memberCnt = memberCnt;
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

	public long getTotalScore() {
		return totalScore;
	}

	public void setTotalScore(long totalScore) {
		this.totalScore = totalScore;
	}

	public long getKickOutTerm() {
		return kickOutTerm;
	}

	public int getServerType() {
		return serverType;
	}
}
