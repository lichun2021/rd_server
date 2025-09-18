package com.hawk.activity.type.impl.bountyHunter.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRandObj;

@HawkConfigManager.XmlResource(file = "activity/bounty_hunter/bounty_hunter_hit.xml")
public class BountyHunterHitCfg extends HawkConfigBase implements HawkRandObj {
	@Id
	private final int id;// ="1"
	private final int weight;// ="30"
	private final int cout;// ="1"
	private final int costMutil;// ="1"
	private final int rewardMutil;// ="1"
	private final int type;

	public BountyHunterHitCfg() {
		id = 0;
		cout = 1;
		weight = 100;
		costMutil = 0;
		rewardMutil = 0;
		type = 1;
	}

	public int getId() {
		return id;
	}

	public int getWeight() {
		return weight;
	}

	public int getCout() {
		return cout;
	}

	public int getCostMutil() {
		return costMutil;
	}

	public int getRewardMutil() {
		return rewardMutil;
	}

	public int getType() {
		return type;
	}

}
