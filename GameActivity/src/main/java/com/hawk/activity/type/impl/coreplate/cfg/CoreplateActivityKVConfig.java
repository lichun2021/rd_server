package com.hawk.activity.type.impl.coreplate.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

@HawkConfigManager.KVResource(file = "activity/coreplate/%s/coreplate_activity_cfg.xml", autoLoad=false, loadParams="283")
public class CoreplateActivityKVConfig extends HawkConfigBase {
	
	private final int serverDelay;
	
	private final int coreplateSocre;
	
	private final int boxScore;
	
	private final int boxAwardId;
	
	private final int buildLevelLimit;
	
	private final String startTimeLimit;
	private final String endTimeLimit;
	
	private long startTimeLimitValue;
	private long endTimeLimitValue;
	
	
	public CoreplateActivityKVConfig(){
		serverDelay = 0;
		coreplateSocre = 1;
		boxScore = 10;
		boxAwardId = 0;
		buildLevelLimit = 0;
		startTimeLimit = "";
		endTimeLimit = "";
	}
	
	@Override
	protected boolean assemble() {
		startTimeLimitValue = HawkTime.parseTime(startTimeLimit);
		endTimeLimitValue = HawkTime.parseTime(endTimeLimit);
		return true;
	}


	public long getServerDelay() {
		return ((long)serverDelay) * 1000;
	}


	public int getBoxScore() {
		return boxScore;
	}



	public int getBoxAwardId() {
		return boxAwardId;
	}



	public int getCoreplateSocre() {
		return coreplateSocre;
	}

	public int getBuildLevelLimit() {
		return buildLevelLimit;
	}

	public long getStartTimeLimitValue() {
		return startTimeLimitValue;
	}

	public void setStartTimeLimitValue(long startTimeLimitValue) {
		this.startTimeLimitValue = startTimeLimitValue;
	}

	public long getEndTimeLimitValue() {
		return endTimeLimitValue;
	}

	public void setEndTimeLimitValue(long endTimeLimitValue) {
		this.endTimeLimitValue = endTimeLimitValue;
	}

	

	

	
	
}
