package com.hawk.game.module.dayazhizhan.battleroom.extry;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZRoomManager.DYZZCAMP;

public class DYZZGamer {
	private String playerId;
	private List<Integer> foggyHeros = new ArrayList<>();
	private List<ArmyInfo> armys = new ArrayList<>();
	private DYZZCAMP camp; // 阵营A B
	private int rewardCount;  //已经获奖次数
	private int seasonScore; //赛季记分
	private int winCount; //胜利次数
	
	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public List<Integer> getFoggyHeros() {
		return foggyHeros;
	}

	public void setFoggyHeros(List<Integer> foggyHeros) {
		this.foggyHeros = foggyHeros;
	}

	public List<ArmyInfo> getArmys() {
		return armys;
	}

	public void setArmys(List<ArmyInfo> armys) {
		this.armys = armys;
	}

	public DYZZCAMP getCamp() {
		return camp;
	}

	public void setCamp(DYZZCAMP camp) {
		this.camp = camp;
	}

	public int getRewardCount() {
		return rewardCount;
	}

	public void setRewardCount(int rewardCount) {
		this.rewardCount = rewardCount;
	}
	
	
	public int getSeasonScore() {
		return seasonScore;
	}
	
	public void setSeasonScore(int seasonScore) {
		this.seasonScore = seasonScore;
	}

	public int getWinCount() {
		return winCount;
	}
	
	public void setWinCount(int winCount) {
		this.winCount = winCount;
	}
}
