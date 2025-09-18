package com.hawk.activity.type.impl.seaTreasure.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRandObj;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * 秘海珍寻高级奖励
 * @author Golden
 *
 */
@HawkConfigManager.XmlResource(file = "activity/sea_treasure/sea_treasure_advanced_award.xml")
public class SeaTreasureAdvancedAwardCfg extends HawkConfigBase implements HawkRandObj {
	/**
	 * id
	 */
	@Id
	private final int advancedRewardId;
	
	/**
	 * 特殊奖励类型
	 */
	private final int advancedType;
	
	/**
	 * 权重
	 */
	private final int weight;
	
	/**
	 * 开启时间
	 */
	private final int openTime;
	
	/**
	 * 奖励
	 */
	private final String reward;
	
	public SeaTreasureAdvancedAwardCfg() {
		advancedRewardId = 0;
		advancedType = 0;
		weight = 0;
		reward = "";
		openTime = 0;
	}

	public int getAdvancedRewardId() {
		return advancedRewardId;
	}

	public int getAdvancedType() {
		return advancedType;
	}

	public int getWeight() {
		return weight;
	}

	public long getOpenTime() {
		return openTime * 1000L;
	}

	@SuppressWarnings("deprecation")
	public List<RewardItem.Builder> getReward() {
		return RewardHelper.toRewardItemList(reward);
	}
}
