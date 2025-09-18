package com.hawk.game.service.tiberium.logunit;

import org.hawk.os.HawkOSOperator;

/**
 * 泰伯利亚玩家战场积分日志信息元
 * 
 * @author z
 *
 */
public class TLWWarResultLogUnit {
	/** 战场房间id */
	private String roomId;
	/** 赛季 */
	private int season;
	/** 期数 */
	private int termId;
	/** 联盟A */
	private String guildA;
	/** 联盟A战场积分 */
	private long scoreA;
	/** 联盟B */
	private String guildB;
	/** 联盟B战场积分 */
	private long scoreB;
	/** 胜利联盟id */
	private String winner;
	/** 机甲首杀联盟id */
	private String firstKill;
	/** 积分首先达到5000的联盟id */
	private String first5000;
	/** 首次占领核心的联盟id */
	private String firstOccupa;
	/** 战斗类型*/
	private int battleType;

	private int serverType;

	public TLWWarResultLogUnit(String roomId, int season, int termId, String guildA, long scoreA, String guildB, long scoreB, String winner, String firstKill, String first5000,
			String firstOccupa,int battleType, int serverType) {
		super();
		this.roomId = roomId;
		this.season = season;
		this.termId = termId;
		this.guildA = guildA;
		this.scoreA = scoreA;
		this.guildB = guildB;
		this.scoreB = scoreB;
		this.winner = winner;
		this.firstKill = firstKill;
		this.first5000 = first5000;
		this.firstOccupa = firstOccupa;
		this.battleType = battleType;
		this.serverType = serverType;
	}

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
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

	public String getWinner() {
		return winner;
	}

	public void setWinner(String winner) {
		this.winner = winner;
	}

	public String getFirstKill() {
		return HawkOSOperator.isEmptyString(firstKill) ? "" : firstKill;
	}

	public void setFirstKill(String firstKill) {
		this.firstKill = firstKill;
	}

	public String getFirst5000() {
		return HawkOSOperator.isEmptyString(first5000) ? "" : first5000;
	}

	public void setFirst5000(String first5000) {
		this.first5000 = first5000;
	}

	public String getFirstOccupa() {
		return HawkOSOperator.isEmptyString(firstOccupa) ? "" : firstOccupa;
	}

	public void setFirstOccupa(String firstOccupa) {
		this.firstOccupa = firstOccupa;
	}
	
	public int getBattleType() {
		return battleType;
	}
	
	public void setBattleType(int battleType) {
		this.battleType = battleType;
	}

	public int getServerType() {
		return serverType;
	}
}
