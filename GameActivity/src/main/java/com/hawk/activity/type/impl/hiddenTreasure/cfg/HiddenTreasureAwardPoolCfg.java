package com.hawk.activity.type.impl.hiddenTreasure.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRandObj;

/**勋章宝藏成就数据
 * @author Winder
 */
@HawkConfigManager.XmlResource(file = "activity/hidden_treasure/hidden_treasure_award_pool.xml")
public class HiddenTreasureAwardPoolCfg extends HawkConfigBase implements HawkRandObj{
	@Id
	private final int rewardId;
	private final int boxRank;
	private final int reward;
	private final int weight;
	
	private final String extraReward;
	

	public HiddenTreasureAwardPoolCfg() {
		rewardId = 0;
		boxRank = 0;
		reward = 0;
		weight = 0;
		extraReward = "";
	}


	public int getRewardId() {
		return rewardId;
	}

	public int getBoxRank() {
		return boxRank;
	}

	public int getReward() {
		return reward;
	}

	@Override
	public int getWeight() {
		return weight;
	}

	public String getExtraReward() {
		return extraReward;
	}

}
