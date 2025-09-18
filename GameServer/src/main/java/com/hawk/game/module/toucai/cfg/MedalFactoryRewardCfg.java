package com.hawk.game.module.toucai.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRandObj;

/**
 * 铁血军团活动时间配置
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/medal_factory_reward.xml")
public class MedalFactoryRewardCfg extends HawkConfigBase implements HawkRandObj {
	// id="10201" library="1" weight="6000" reward="30000_850026_20,30000_1510002_50"

	@Id
	private final int id;// ="10101"
	private final int library;// ="4940001"
	private final int weight;// ="1"
	private final String reward;// ="30000_15900001_1"
	private final int stealTime;
	private final String stealReward;
	private final int stealNumMax;
	
	public MedalFactoryRewardCfg() {
		id = 0;
		library = 0;
		weight = 0;
		reward = "";
		stealTime = 0;
		stealReward = "";
		stealNumMax=0;
	}

	public int getId() {
		return id;
	}

	public int getLibrary() {
		return library;
	}

	@Override
	public int getWeight() {
		return weight;
	}

	public String getReward() {
		return reward;
	}

	public int getStealTime() {
		return stealTime;
	}

	public String getStealReward() {
		return stealReward;
	}

	public int getStealNumMax() {
		return stealNumMax;
	}

}
