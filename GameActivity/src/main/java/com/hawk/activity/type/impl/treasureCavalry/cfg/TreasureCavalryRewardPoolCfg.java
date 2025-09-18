package com.hawk.activity.type.impl.treasureCavalry.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRandObj;

@HawkConfigManager.XmlResource(file = "activity/treasure_cavalry/treasure_cavalry_reward_pool.xml")
public class TreasureCavalryRewardPoolCfg extends HawkConfigBase implements HawkRandObj {
	@Id
	private final int id;// ="1"
	private final int weight;// ="30"

	public TreasureCavalryRewardPoolCfg() {
		id = 0;
		weight = 100;
	}

	public int getId() {
		return id;
	}

	@Override
	public int getWeight() {
		return weight;
	}

}
