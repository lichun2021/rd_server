package com.hawk.game.strengthenguide.entity;

import com.hawk.game.protocol.StrengthenGuide.StrengthenGuideType;
/**
 * 我要变强 玩家各个模块的评分 在总动 0~99段 的 位置
 * @author RickMei
 *
 */
public class SGPlayerEntity {
	
	private String playerId;
	
	private int buildScoreIndex = 0;
	
	private int soldierScoreIndex = 0;
	
	private int heroScoreIndex = 0;
	
	private int scienceScoreIndex = 0;
	
	private int talentScoreIndex = 0;

	private int buildScore = 0;
	
	private int soldierScore = 0;
	
	private int heroScore = 0;
	
	private int scienceScore = 0;
	
	private int talentScore = 0;
	
	private int totalScore = 0;

	private int totalIndex = 0;
	
	public String getPlayerId() {
		return playerId;
	}

	public int getBuildScoreIndex() {
		return buildScoreIndex;
	}

	public int getTotalScore() {
		return totalScore;
	}

	public void setTotalScore(int totalScore) {
		this.totalScore = totalScore;
	}

	public int getTotalIndex() {
		return totalIndex;
	}

	public void setTotalIndex(int totalIndex) {
		this.totalIndex = totalIndex;
	}

	public int getSoldierScoreIndex() {
		return soldierScoreIndex;
	}

	public int getHeroScoreIndex() {
		return heroScoreIndex;
	}

	public int getScienceScoreIndex() {
		return scienceScoreIndex;
	}

	public int getTalentScoreIndex() {
		return talentScoreIndex;
	}

	public int getBuildScore() {
		return buildScore;
	}

	public int getSoldierScore() {
		return soldierScore;
	}

	public int getHeroScore() {
		return heroScore;
	}

	public int getScienceScore() {
		return scienceScore;
	}

	public int getTalentScore() {
		return talentScore;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public void setBuildScoreIndex(int buildScoreIndex) {
		this.buildScoreIndex = buildScoreIndex;
	}

	public void setSoldierScoreIndex(int soldierScoreIndex) {
		this.soldierScoreIndex = soldierScoreIndex;
	}

	public void setHeroScoreIndex(int heroScoreIndex) {
		this.heroScoreIndex = heroScoreIndex;
	}

	public void setScienceScoreIndex(int scienceScoreIndex) {
		this.scienceScoreIndex = scienceScoreIndex;
	}

	public void setTalentScoreIndex(int talentScoreIndex) {
		this.talentScoreIndex = talentScoreIndex;
	}

	public void setBuildScore(int buildScore) {
		this.buildScore = buildScore;
	}

	public void setSoldierScore(int soldierScore) {
		this.soldierScore = soldierScore;
	}

	public void setHeroScore(int heroScore) {
		this.heroScore = heroScore;
	}

	public void setScienceScore(int scienceScore) {
		this.scienceScore = scienceScore;
	}

	public void setTalentScore(int talentScore) {
		this.talentScore = talentScore;
	}

	public int getScoreByType(StrengthenGuideType sgType){
		if(StrengthenGuideType.Army == sgType){
			return getSoldierScore();
		}else if(StrengthenGuideType.Building == sgType){
			return getBuildScore();
		}else if(StrengthenGuideType.Hero == sgType){
			return getHeroScore();
		}else if(StrengthenGuideType.Science == sgType){
			return getScienceScore();
		}else if(StrengthenGuideType.Commander == sgType){
			return getTalentScore();
		}else{
			return getTotalScore();
		}
	}
	
	public void setScoreByType(StrengthenGuideType sgType, int score){
		if(StrengthenGuideType.Army == sgType){
			this.soldierScore = score;
		}else if(StrengthenGuideType.Building == sgType){
			this.buildScore = score;
		}else if(StrengthenGuideType.Hero == sgType){
			this.heroScore = score;
		}else if(StrengthenGuideType.Science == sgType){
			this.scienceScore = score;
		}else if(StrengthenGuideType.Commander == sgType){
			this.talentScore = score;
		}else{
			this.totalScore = score;
		}
	}
	
	
	public int getScoreIndexByType(StrengthenGuideType sgType){
		if(StrengthenGuideType.Army == sgType){
			return getSoldierScoreIndex();
		}else if(StrengthenGuideType.Building == sgType){
			return getBuildScoreIndex();
		}else if(StrengthenGuideType.Hero == sgType){
			return getHeroScoreIndex();
		}else if(StrengthenGuideType.Science == sgType){
			return getScienceScoreIndex();
		}else if(StrengthenGuideType.Commander == sgType){
			return getTalentScoreIndex();
		}else{
			return getTotalIndex();
		}
	}
	
	public void setScoreIndexByType(StrengthenGuideType sgType, int scoreIndex){
		if(StrengthenGuideType.Army == sgType){
			this.soldierScoreIndex = scoreIndex;
		}else if(StrengthenGuideType.Building == sgType){
			this.buildScoreIndex = scoreIndex;
		}else if(StrengthenGuideType.Hero == sgType){
			this.heroScoreIndex = scoreIndex;
		}else if(StrengthenGuideType.Science == sgType){
			this.scienceScoreIndex = scoreIndex;
		}else if(StrengthenGuideType.Commander == sgType){
			this.talentScoreIndex = scoreIndex;
		}else{
			this.totalIndex = scoreIndex;
		}
	}
	
	public static SGPlayerEntity valueOf(String playerId){
		 SGPlayerEntity entity = new SGPlayerEntity();
		 entity.setPlayerId(playerId);
		 return entity;
	}
	
	public int getTotalNewScore(){
		return (getSoldierScore() + getTalentScore() + getTalentScore() + getBuildScore() + getHeroScore() ) / 5;
	}
}
