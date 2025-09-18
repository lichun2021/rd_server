package com.hawk.game.module.dayazhizhan.battleroom.extry;

import org.hawk.os.HawkOSOperator;

import com.hawk.game.protocol.World.PBDYZZFameHallMember;

public class DYZZBattleRoomFameHallMember {

	private String playerId;
	
	private String serverId;
	
	private String playerName;

	private int icon;
	
	private String pficon;
	
	private int rank;
	
	private int score;
	
	
	
	public PBDYZZFameHallMember createPBDYZZFameHallMember(){
		PBDYZZFameHallMember.Builder builder = PBDYZZFameHallMember.newBuilder();
		builder.setPlayerId(this.playerId);
		builder.setPlayerName(this.playerName);
		builder.setServerId(this.serverId);
		builder.setIcon(this.icon);
		if(HawkOSOperator.isEmptyString(this.pficon)){
			builder.setPfIcon(this.pficon);
		}
		builder.setRank(this.rank);
		builder.setScore(this.score);
		return builder.build();
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	public String getPficon() {
		return pficon;
	}

	public void setPficon(String pficon) {
		this.pficon = pficon;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}
	
	
	
}
