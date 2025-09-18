package com.hawk.activity.type.impl.bountyHunter.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRandObj;

@HawkConfigManager.XmlResource(file = "activity/bounty_hunter/bounty_hunter_reward.xml")
public class BountyHunterRewardCfg extends HawkConfigBase implements HawkRandObj {
	@Id
	private final int id;// ="1"
	private final int pool;// ="1"
	private final int weight;// ="30"
	private final String reward;// ="10000_1010_100"

	public BountyHunterRewardCfg() {
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
