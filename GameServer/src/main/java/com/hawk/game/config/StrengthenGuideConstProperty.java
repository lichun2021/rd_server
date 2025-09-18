package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "xml/stronger_const.xml")
public class StrengthenGuideConstProperty extends HawkConfigBase{

	private static StrengthenGuideConstProperty instance = null;
	//最大分数
	protected final int scoreMax;
	//每个段间隔分数
	protected final int scoreInterval;
	//总段数
	protected final int scoreStages;
	
	//士兵得分要除以
	protected final int soldierMutiple;
	
	//多少主堡等级解锁
	protected final int functionUnlockLevel;
	
	public StrengthenGuideConstProperty(){
		instance = this;
		scoreMax = 1000;
		scoreInterval = 10;
		scoreStages = 100;
		soldierMutiple = 10000;
		functionUnlockLevel = 10;
	}
	
	public int getSoldierMutiple() {
		return soldierMutiple;
	}

	public int getFunctionUnlockLevel() {
		return functionUnlockLevel;
	}

	public static StrengthenGuideConstProperty getInstance(){
		return instance;
	}

	public int getScoreMax() {
		return scoreMax;
	}

	public int getScoreInterval() {
		return scoreInterval;
	}

	public int getScoreStages() {
		return scoreStages;
	}
	
	public int getScoreIndex( int score ){
		int index = score / scoreInterval;
		if(index < 0){
			index = 0;
		}
		if(index >= scoreStages){
			index = scoreStages - 1;
		}
		return index;
	}
}