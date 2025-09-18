package com.hawk.activity.type.impl.snowball.cfg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * 雪球大战积分奖励配置
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "activity/snowball/snowball_score_reward.xml")
public class SnowballScoreRewardCfg extends HawkConfigBase {
	
	@Id
	private final int id;
	
	/**
	 * 目标积分
	 */
	private final int target;

	/**
	 * 奖励
	 */
	private final String reward;

	/**
	 * 奖励列表
	 */
	private List<RewardItem.Builder> rewardList;
	
	/**
	 * 构造
	 */
	public SnowballScoreRewardCfg() {
		id = 0;
		target = 0;
		reward = "";
	}

	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}

	public int getId() {
		return id;
	}

	public int getTarget() {
		return target;
	}

	public String getReward() {
		return reward;
	}

	@Override
	protected boolean assemble() {
		
		List<RewardItem.Builder> rewardList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(reward)) {
			rewardList = RewardHelper.toRewardItemImmutableList(reward);
		}
		this.rewardList = rewardList;
		
		return true;
	}
}
