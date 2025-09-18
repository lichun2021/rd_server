package com.hawk.game.module.dayazhizhan.marchserver.service;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.module.dayazhizhan.playerteam.cfg.DYZZWarCfg;
import com.hawk.game.module.dayazhizhan.playerteam.service.DYZZMatchData;

public class DYZZMatchCamp {

	
	private List<DYZZMatchData> teams =  new ArrayList<>();

	private int martchTotalScore;
	
	public List<DYZZMatchData> getCampTeams() {
		return this.teams;
	}

	public void setCampTeams(List<DYZZMatchData> campATeams) {
		this.teams = campATeams;
	}
	
	public int getMartchTotalScore() {
		return martchTotalScore;
	}
	
	
	public int getCampMemberCount(){
		int count = 0;
		for(DYZZMatchData team : this.teams){
			count += team.getMembers().size();
		}
		return count;
	}
	
	
	public boolean addToCamp(DYZZMatchData data){
		DYZZWarCfg cfg = HawkConfigManager.getInstance().getKVInstance(DYZZWarCfg.class);
		int memberCount = this.getCampMemberCount();
		int needCount = cfg.getTeamMemberCount() - memberCount;
		if(needCount >= data.getMemberCount()){
			this.teams.add(data);
			return true;
		}
		return false;
	}
	
	

	public boolean matchCampFinish(){
		DYZZWarCfg cfg = HawkConfigManager.getInstance().getKVInstance(DYZZWarCfg.class);
		int count = this.getCampMemberCount();
		if(count >= cfg.getTeamMemberCount()){
			return true;
		}
		return false;
	}
	
	private void calMatchScore(){
		int score = 0;
		for(DYZZMatchData data : this.teams){
			score += data.getTotalMatchScore();
		}
		this.martchTotalScore = score;
	}
}
