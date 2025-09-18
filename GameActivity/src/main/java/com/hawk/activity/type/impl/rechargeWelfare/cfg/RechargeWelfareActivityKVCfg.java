package com.hawk.activity.type.impl.rechargeWelfare.cfg;

import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.serialize.string.SerializeHelper;
@HawkConfigManager.KVResource(file = "activity/recharge_welfare/recharge_welfare_cfg.xml")
public class RechargeWelfareActivityKVCfg extends HawkConfigBase {
	private final int serverDelay;
	
	private final String extReward;
	
	private final String itemOnce;
	
	private final String itemTen;
	
	private final String itemPrice;
	//每日免费单抽次数
	private final int dailyFreeTimes;
	//活动期间每日抽卡次数上限限制
	private final int  dailyTimesLimit;
	//各卡池须选择的奖励种类数量
	private final String poolChooseItems;
	
	// 每充值1元获取X个专属道具
	private final int rechargeGainItem;
	
	//# 每日可触发充值奖励次数上限
	private final int dailyRechargeGainItemLimit;
	
	// 每日内置任务条件参数（日常任务积分点数）
	private final int dailyQuestCondition;

	// 每日内置任务领取奖励内容
	private final String dailyQuestReward;
	
	private List<RewardItem.Builder> extRewardList;
	
	private List<RewardItem.Builder> dailyQuestRewardList;
	
	private Map<Integer, Integer> poolChooseItemsMap;
	
	
	public RechargeWelfareActivityKVCfg() {
		this.serverDelay = 0;
		this.extReward = "";
		this.itemOnce = "";
		this.itemTen = "";
		this.itemPrice = "";
		this.dailyFreeTimes = 0;
		this.dailyTimesLimit = 0;
		this.poolChooseItems ="";
		this.rechargeGainItem = 0;
		this.dailyRechargeGainItemLimit = 0;
		this.dailyQuestCondition = 0;
		this.dailyQuestReward = "";
	}
	

	@Override
	protected boolean assemble() {
		try {
			extRewardList = RewardHelper.toRewardItemImmutableList(extReward);
			dailyQuestRewardList = RewardHelper.toRewardItemImmutableList(dailyQuestReward);
			poolChooseItemsMap = SerializeHelper.cfgStr2Map(poolChooseItems);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}


	public List<RewardItem.Builder> getExtRewardList() {
		return extRewardList;
	}


	public void setExtRewardList(List<RewardItem.Builder> extRewardList) {
		this.extRewardList = extRewardList;
	}


	public List<RewardItem.Builder> getDailyQuestRewardList() {
		return dailyQuestRewardList;
	}


	public void setDailyQuestRewardList(List<RewardItem.Builder> dailyQuestRewardList) {
		this.dailyQuestRewardList = dailyQuestRewardList;
	}


	public long getServerDelay() {
		return ((long)serverDelay) * 1000;
	}

	public String getExtReward() {
		return extReward;
	}


	public String getItemOnce() {
		return itemOnce;
	}


	public String getItemTen() {
		return itemTen;
	}

	public String getItemPrice() {
		return itemPrice;
	}

	public int getDailyFreeTimes() {
		return dailyFreeTimes;
	}

	public int getDailyTimesLimit() {
		return dailyTimesLimit;
	}

	public String getPoolChooseItems() {
		return poolChooseItems;
	}


	public int getRechargeGainItem() {
		return rechargeGainItem;
	}


	public int getDailyRechargeGainItemLimit() {
		return dailyRechargeGainItemLimit;
	}


	public int getDailyQuestCondition() {
		return dailyQuestCondition;
	}


	public String getDailyQuestReward() {
		return dailyQuestReward;
	}


	public Map<Integer, Integer> getPoolChooseItemsMap() {
		return poolChooseItemsMap;
	}


	public void setPoolChooseItemsMap(Map<Integer, Integer> poolChooseItemsMap) {
		this.poolChooseItemsMap = poolChooseItemsMap;
	}


}
