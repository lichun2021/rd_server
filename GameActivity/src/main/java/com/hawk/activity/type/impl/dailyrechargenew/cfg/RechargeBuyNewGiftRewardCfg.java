package com.hawk.activity.type.impl.dailyrechargenew.cfg;

import java.util.List;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * 今日累充特价礼包奖励配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "activity/recharge_buy_new/recharge_buy_new_reward.xml")
public class RechargeBuyNewGiftRewardCfg extends HawkConfigBase {
	// 奖励ID
	@Id
	private final int id;
	// 对应奖励类别ID
	private final int jackpotId;
	// 奖励内容
	private final String reward;
	
	private List<RewardItem.Builder> rewardList;

	public RechargeBuyNewGiftRewardCfg() {
		id = 0;
		jackpotId = 0;
		reward = "";
	}
	
	@Override
	protected boolean assemble() {
		rewardList = RewardHelper.toRewardItemImmutableList(reward);
		return true;
	}

	public int getId() {
		return id;
	}

	public int getRewardType() {
		return jackpotId;
	}

	public String getReward() {
		return reward;
	}

	public List<RewardItem.Builder> getRewardList() {
		return rewardList.stream().map(e -> e.clone()).collect(Collectors.toList());
	}
}
