package com.hawk.game.module.dayazhizhan.battleroom.extry;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class DYZZExtraParam {
	private String battleId = "";

	/** 游戏参与玩家*/
	private List<DYZZGamer> gamers = new ArrayList<>();
	
	/**
	 * 赛季期数
	 */
	private int seasonTerm;

	/**
	 * 名人堂
	 */
	private List<DYZZBattleRoomFameHallMember> fameHallMembers = new ArrayList<>();
	
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("battleId", battleId)
				.toString();
	}

	public String getBattleId() {
		return battleId;
	}

	public void setBattleId(String battleId) {
		this.battleId = battleId;
	}

	public List<DYZZGamer> getGamers() {
		return gamers;
	}

	public void setGamers(List<DYZZGamer> gamers) {
		this.gamers = gamers;
	}

	public void addGamer(DYZZGamer gamer){
		gamers.add(gamer);
	}
	
	public DYZZGamer getGamer(String pid) {
		for(DYZZGamer gamer: gamers){
			if(Objects.equal(pid, gamer.getPlayerId())){
				return gamer;
			}
		}
		return null;
	}
	
	
	public void setSeasonTerm(int seasonTerm) {
		this.seasonTerm = seasonTerm;
	}
	
	public int getSeasonTerm() {
		return seasonTerm;
	}
	
	public List<DYZZBattleRoomFameHallMember> getFameHallMembers() {
		return fameHallMembers;
	}
	
	public void setFameHallMembers(List<DYZZBattleRoomFameHallMember> fameHallMembers) {
		this.fameHallMembers = fameHallMembers;
	}

}
