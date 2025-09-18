package com.hawk.activity.type.impl.heroSkin.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRandObj;

@HawkConfigManager.XmlResource(file = "activity/hero_skin_lottery/hero_skin_lottery_reward.xml")
public class HeroSkinRewardCfg extends HawkConfigBase implements HawkRandObj {
	@Id
	private final int id;// ="1"
	private final int pool;// ="1"
	private final int weight;// ="30"
	private final String reward;// ="10000_1010_100"
	private final int finallyReward;
	
	public HeroSkinRewardCfg() {
		id = 0;
		pool = 0;
		weight = 100;
		reward = "";
		finallyReward = 0;
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

	public boolean isFinallyReward() {
		return finallyReward == 1;
	}
}
