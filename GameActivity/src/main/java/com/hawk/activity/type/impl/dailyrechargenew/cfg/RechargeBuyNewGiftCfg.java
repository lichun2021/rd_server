package com.hawk.activity.type.impl.dailyrechargenew.cfg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * 今日累充特价礼包信息配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "activity/recharge_buy_new/recharge_buy_new_gift.xml")
public class RechargeBuyNewGiftCfg extends HawkConfigBase {
	// 奖励ID
	@Id
	private final int giftId;
	// 礼包对应的成就ID
	private final int achieveId;
	// 购买礼包消耗
	private final String cost;
	// 礼包固有奖励
	private final String buyReward;
	// 礼包选择性奖励
	private final String chooseItem;
	
	private List<RewardItem.Builder> costItems;
	// 固有奖励信息
	private List<RewardItem.Builder> rewardList;
	// 选择性奖励信息
	private Map<Integer, Integer> chooseRewardMap = new HashMap<Integer, Integer>();
	// 选择奖励的总个数
	private int chooseRewardCount;

	public RechargeBuyNewGiftCfg() {
		giftId = 0;
		achieveId = 0;
		cost = "";
		buyReward = "";
		chooseItem = "";
	}
	
	public int getGiftId() {
		return giftId;
	}

	public int getAchieveId() {
		return achieveId;
	}

	public String getCost() {
		return cost;
	}

	public String getBuyReward() {
		return buyReward;
	}

	public String getChooseItem() {
		return chooseItem;
	}

	@Override
	protected boolean assemble() {
		costItems = RewardHelper.toRewardItemImmutableList(cost);
		rewardList = RewardHelper.toRewardItemImmutableList(buyReward);
		chooseRewardCount = 0;
		String[] chooseStrArr = chooseItem.split(",");
		for (String str : chooseStrArr) {
			String[] itemStr = str.split("_");
			int chooseId = Integer.parseInt(itemStr[0]);
			chooseRewardMap.put(chooseId, Integer.parseInt(itemStr[1]));
			chooseRewardCount += chooseRewardMap.get(chooseId);
		}
		
		return true;
	}
	
	public List<RewardItem.Builder> getRewardList() {
		return rewardList.stream().map(e -> e.clone()).collect(Collectors.toList());
	}
	
	public Set<Integer> getChooseIds() {
		return chooseRewardMap.keySet();
	}
	
	public int getChooseIdCount(int chooseId) {
		return chooseRewardMap.getOrDefault(chooseId, 0);
	}
	
	public int getChooseRewardCount() {
		return chooseRewardCount;
	}

	public List<RewardItem.Builder> getCostItems() {
		return costItems.stream().map(e -> e.clone()).collect(Collectors.toList());
	}

}
