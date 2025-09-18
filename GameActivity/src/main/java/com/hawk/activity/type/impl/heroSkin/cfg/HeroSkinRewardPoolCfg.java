package com.hawk.activity.type.impl.heroSkin.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRandObj;

@HawkConfigManager.XmlResource(file = "activity/hero_skin_lottery/hero_skin_lottery_reward_pool.xml")
public class HeroSkinRewardPoolCfg extends HawkConfigBase implements HawkRandObj {
	@Id
	private final int id;// ="1"
	private final int weight;// ="30"

	public HeroSkinRewardPoolCfg() {
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
