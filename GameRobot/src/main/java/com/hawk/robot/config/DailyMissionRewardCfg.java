package com.hawk.robot.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "xml/dailyActiveReward.xml")
public class DailyMissionRewardCfg extends HawkConfigBase{
	
	private final int id;
	private final int factoryLevel;
	private final int giftId;
	private final String award;
	
	public DailyMissionRewardCfg() {
		id = 0;
		factoryLevel = 1;
		giftId = 1;
		award = "";
	}

	public int getId() {
		return id;
	}

	public String getAward() {
		return award;
	}

	public int getFactoryLevel() {
		return factoryLevel;
	}

	public int getGiftId() {
		return giftId;
	}
}
