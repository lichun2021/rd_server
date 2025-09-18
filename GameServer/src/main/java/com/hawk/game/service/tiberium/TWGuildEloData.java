package com.hawk.game.service.tiberium;

/**
 * 泰伯利亚Elo积分数据
 * 
 * @author admin
 *
 */
public class TWGuildEloData {

	public String id;

	public int score;
	
	public String serverId;
	
	//上次活跃期数
	private int lastActiveTerm;
	
	/** 已结算赛季*/
	public int calcedSeason;
	
	public TWGuildEloData() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public int getCalcedSeason() {
		return calcedSeason;
	}

	public void setCalcedSeason(int calcedSeason) {
		this.calcedSeason = calcedSeason;
	}

	public int getLastActiveTerm() {
		return lastActiveTerm;
	}

	public void setLastActiveTerm(int lastActiveTerm) {
		this.lastActiveTerm = lastActiveTerm;
	}

	@Override
	public String toString() {
		return "TWGuildEloData [id=" + id + ", score=" + score + ", serverId=" + serverId + ", lastActiveTerm=" + lastActiveTerm + ", calcedSeason=" + calcedSeason + "]";
	}
}
