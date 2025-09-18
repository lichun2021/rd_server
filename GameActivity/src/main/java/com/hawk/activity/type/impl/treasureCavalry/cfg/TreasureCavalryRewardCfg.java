package com.hawk.activity.type.impl.treasureCavalry.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRandObj;

@HawkConfigManager.XmlResource(file = "activity/treasure_cavalry/treasure_cavalry_reward.xml")
public class TreasureCavalryRewardCfg extends HawkConfigBase implements HawkRandObj {
	@Id
	private final int id;// ="1"
	private final int pool;// ="1"
	private final int weight;// ="30"
	private final String reward;// ="10000_1010_100"

	public TreasureCavalryRewardCfg() {
		id = 0;
		pool = 0;
		weight = 100;
		reward = "";
	}

	public int getId() {
		return id;
	}

	public int getPool() {
		return pool;
	}

	@Override
	public int getWeight() {
		return weight;
	}

	public String getReward() {
		return reward;
	}

}
