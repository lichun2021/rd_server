package com.hawk.activity.type.impl.resourceDefense.cfg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * 资源保卫战奖励配置
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "activity/resource_defense/resource_defense_rewards.xml")
public class ResourceDefenseRewardsCfg extends HawkConfigBase {

	@Id
	private final int id;
	
	/**
	 * 等级
	 */
	private final int level;
	
	/**
	 * 奖励
	 */
	private final String rewards;
	
	/**
	 * 是否是高级奖励 1 普通 2 高级
	 */ 
	private final int quality;
	
	/**
	 * 奖励列表
	 */
	private List<RewardItem.Builder> rewardList;
	
	public ResourceDefenseRewardsCfg() {
		id = 0;
		level= 0;
		rewards = "";
		quality = 0;
	}

	public int getId() {
		return id;
	}

	public int getLevel() {
		return level;
	}

	public String getRewards() {
		return rewards;
	}

	public int getQuality() {
		return quality;
	}
	
	@Override
	protected boolean assemble() {
		List<RewardItem.Builder> rewardList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(rewards)) {
			rewardList = RewardHelper.toRewardItemImmutableList(rewards);
		}
		this.rewardList = rewardList;
		return true;
	}

	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}
	
}
