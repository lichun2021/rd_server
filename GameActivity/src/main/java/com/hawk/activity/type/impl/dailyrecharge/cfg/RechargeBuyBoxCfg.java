package com.hawk.activity.type.impl.dailyrecharge.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * 今日累充活动配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "activity/recharge_buy/recharge_buy_box.xml")
public class RechargeBuyBoxCfg extends HawkConfigBase {
	
	// 宝箱ID
	@Id
	private final int boxId;
	/** 成就id*/
	private final int achieveId;
	/** 宝箱价格 */
	private final String cost;
	/** 宝箱奖励 */
	private final String buyrewards;
	
	private List<RewardItem.Builder> price;
	private List<RewardItem.Builder> rewardList;

	public RechargeBuyBoxCfg() {
		boxId = 0;
		achieveId = 0;
		cost = "";
		buyrewards = "";
	}
	
	@Override
	protected boolean assemble() {
		price = RewardHelper.toRewardItemImmutableList(cost);
		rewardList = RewardHelper.toRewardItemImmutableList(buyrewards);
		return true;
	}

	public int getBoxId() {
		return boxId;
	}

	public int getAchieveId() {
		return achieveId;
	}

	public String getCost() {
		return cost;
	}

	public String getBuyrewards() {
		return buyrewards;
	}
	
	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}
	
	public List<RewardItem.Builder> getPrice() {
		return price;
	}
	
}
