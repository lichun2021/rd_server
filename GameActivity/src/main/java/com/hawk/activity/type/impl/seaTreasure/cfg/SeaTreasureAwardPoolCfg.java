package com.hawk.activity.type.impl.seaTreasure.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRandObj;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * 秘海珍寻奖池配置
 * @author Golden
 *
 */
@HawkConfigManager.XmlResource(file = "activity/sea_treasure/sea_treasure_award_pool.xml")
public class SeaTreasureAwardPoolCfg extends HawkConfigBase implements HawkRandObj {
	/**
	 * id
	 */
	@Id
	private final int rewardId;
	
	/**
	 * 是否是特殊奖励
	 */
	private final int isAdvancedReward;

	/**
	 * 特殊奖励类型
	 */
	private final int advancedType;
	
	/**
	 * 奖励时间
	 */
	private final int rewardTime;
	
	/**
	 * 开启次数限制
	 */
	private final int timesLimit;
	
	/**
	 * 权重
	 */
	private final int weight;
	
	/**
	 * 奖励
	 */
	private final String reward;
	
	public SeaTreasureAwardPoolCfg() {
		rewardId = 0;
		isAdvancedReward = 0;
		advancedType = 0;
		rewardTime = 0;
		timesLimit = 0;
		weight = 0;
		reward = "";
	}

	public int getRewardIdId() {
		return rewardId;
	}

	public boolean isAdvancedReward() {
		return isAdvancedReward > 0;
	}

	public int getAdvancedType() {
		return advancedType;
	}

	public int getRewardTime() {
		return rewardTime;
	}

	public int getTimesLimit() {
		return timesLimit;
	}

	public int getWeight() {
		return weight;
	}
	
	@SuppressWarnings("deprecation")
	public List<RewardItem.Builder> getReward() {
		return RewardHelper.toRewardItemList(reward);
	}
}
