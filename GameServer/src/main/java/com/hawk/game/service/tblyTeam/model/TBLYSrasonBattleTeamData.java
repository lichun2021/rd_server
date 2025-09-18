package com.hawk.game.service.tblyTeam.model;

import java.util.HashSet;
import java.util.Set;

import com.hawk.game.service.guildTeam.model.GuildTeamData;


public class TBLYSrasonBattleTeamData {
	
	private String teamId;
	
	private GuildTeamData teamData;
	
	private String battleRoomId;
	
	private Set<String> teamPlayers;
	
	public TBLYSrasonBattleTeamData(String teamId,String battleId){
		this.teamId = teamId;
		this.battleRoomId = battleId;
		this.teamPlayers = new HashSet<>();
	}
	
	
	
	public String getTeamId() {
		return teamId;
	}
		
	
	public String getBattleRoomId() {
		return battleRoomId;
	}
	
	
	public GuildTeamData getTeamData() {
		return teamData;
	}
	
	public void updateTeamData(GuildTeamData teamData){
		this.teamData = teamData;
	}
	
	public void updateTeamPlayers(Set<String> players){
		Set<String> set = new HashSet<>();
		set.addAll(players);
		this.teamPlayers = set;
	}
	
	
	public Set<String> getTeamPlayers() {
		return teamPlayers;
	}
}
