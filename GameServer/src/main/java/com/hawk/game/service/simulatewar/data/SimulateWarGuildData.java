package com.hawk.game.service.simulatewar.data;


/**
 * 攻防模拟战的工会信息.
 * 
 * @author jm
 *
 */
public class SimulateWarGuildData {
	/**
	 * 工会ID。
	 */
	private String guildId;
	/**
	 * 工会简称
	 */
	private String guildTag;
	/**
	 * 工会名字.
	 */
	private String guildName;
	/**
	 * 助威次数.
	 */
	private int encourageTimes;
	/**
	 * 整个联盟的战力.
	 */
	private long battleValue;
	/**
	 * 参加的队伍数.
	 */
	private int teamNum;
	/**
	 * 战斗记录ID。
	 */
	private String battleId;
	/**
	 * 公会旗帜
	 */
	private int guildFlag;
	
	public String getGuildId() {
		return guildId;
	}
	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}
	public String getGuildTag() {
		return guildTag;
	}
	public void setGuildTag(String guildTag) {
		this.guildTag = guildTag;
	}
	
	public int getEncourageTimes() {
		return encourageTimes;
	}
	public void setEncourageTimes(int encourageTimes) {
		this.encourageTimes = encourageTimes;
	}
	public long getBattleValue() {
		return battleValue;
	}
	public void setBattleValue(long battleValue) {
		this.battleValue = battleValue;
	}
	public int getTeamNum() {
		return teamNum;
	}
	public void setTeamNum(int teamNum) {
		this.teamNum = teamNum;
	}
	
	public  void addTeamNum(int teamNum) {
		this.teamNum += teamNum;		
		if (teamNum <= 0) {
			teamNum = 0;
		}
	}
	
	public void addBattleValue(long addValue) {
		this.battleValue = this.battleValue + addValue;		
		if (battleValue <= 0) {
			this.battleValue = 0;
		}
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
}
