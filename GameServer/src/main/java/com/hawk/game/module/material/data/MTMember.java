package com.hawk.game.module.material.data;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildChampionship.PBChampionPlayer;
import com.hawk.game.protocol.MaterialTransport.PBMTPlayer;

public class MTMember {
	private String playerId = "";
	private String name = "";
	private int icon;
	private String pfIconf = "";
	private String server = "";
	private long power;
	private PBChampionPlayer battleData = PBChampionPlayer.getDefaultInstance();
	private int robState;
	public static MTMember valueOf(Player leader , PBChampionPlayer battleData) {
		if(battleData==null){
			throw new RuntimeException("battle data null");
		}
		MTMember member = new MTMember();
		member.setIcon(leader.getIcon());
		member.setName(leader.getName());
		member.setPfIconf(leader.getPfIcon());
		member.setPlayerId(leader.getId());
		member.setServer(leader.getServerId());
		member.setPower(leader.getPower());
		member.setBattleData(battleData);
		return member;
	}

	public PBMTPlayer toPBObj() {
		PBMTPlayer.Builder builder = PBMTPlayer.newBuilder();
		builder.setPlayerId(playerId);
		builder.setName(name);
		builder.setIcon(icon);
		builder.setPfIconf(pfIconf);
		builder.setServer(server);
		builder.setPower(power);
		builder.setBattleData(battleData);
		builder.setRobState(robState);
		return builder.build();
	}

	public void mergeFrom(PBMTPlayer obj) {
		this.playerId = obj.getPlayerId();
		this.name = obj.getName();
		this.icon = obj.getIcon();
		this.pfIconf = obj.getPfIconf();
		this.server = obj.getServer();
		this.power = obj.getPower();
		this.battleData = obj.getBattleData();
		this.robState = obj.getRobState();
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	public String getPfIconf() {
		return pfIconf;
	}

	public void setPfIconf(String pfIconf) {
		this.pfIconf = pfIconf;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public long getPower() {
		return power;
	}

	public void setPower(long power) {
		this.power = power;
	}

	public PBChampionPlayer getBattleData() {
		return battleData;
	}

	public void setBattleData(PBChampionPlayer battleData) {
		this.battleData = battleData;
	}

	public int getRobState() {
		return robState;
	}

	public void setRobState(int robState) {
		this.robState = robState;
	}

}
