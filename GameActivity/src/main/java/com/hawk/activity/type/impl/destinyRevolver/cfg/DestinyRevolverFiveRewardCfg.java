package com.hawk.activity.type.impl.destinyRevolver.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRandObj;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * 命运左轮外层5个格子的奖励配置
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "activity/destiny_revolver/destiny_revolver_five_reward.xml")
public class DestinyRevolverFiveRewardCfg extends HawkConfigBase implements HawkRandObj {
	
	@Id
	private final int id;
	
	private final int weight;
	
	private final String reward;
	
	private final int jackpot;
	
	private List<RewardItem.Builder> rewardList;
	
	public DestinyRevolverFiveRewardCfg() {
		id = 0;
		weight = 0;
		reward = "";
		jackpot = 0;
	}

	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}

	public int getId() {
		return id;
	}

	public int getWeight() {
		return weight;
	}

	public String getReward() {
		return reward;
	}
	
	public int getJackpot() {
		return jackpot;
	}

	@Override
	protected boolean assemble() {
		rewardList = RewardHelper.toRewardItemImmutableList(reward);
		return true;
	}	
}
