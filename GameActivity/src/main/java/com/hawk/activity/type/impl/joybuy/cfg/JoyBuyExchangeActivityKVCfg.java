package com.hawk.activity.type.impl.joybuy.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;
import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.KVResource(file = "activity/joy_exchange/joy_exchange_cfg.xml")
public class JoyBuyExchangeActivityKVCfg extends HawkConfigBase {
	//服务器开服延时开启活动时间；单位：秒
	private final int serverDelay;
	// 道具专属兑换道具
	private final String specItem;

	// 兑换奖池随机权重
	private final String exchangePool;

	//兑换奖励每日重置时间节点（整点）
	private final String resetTimeList;

	// 每次重置后，各刷新次数对应消耗
	private final String refreshCost;
	
	//每日兑换次数
	private final int dailyExchangeTimes;
	
	//兑换道具列表
	private List<RewardItem.Builder> specItemAwardList;
	//刷新消耗列表
	private List<RewardItem.Builder> refreshCostList;
	//重置刷新时间
	private int[] resetTimeAry;
	//奖池组
	private int[][] exchangePoolList;

	public JoyBuyExchangeActivityKVCfg() {
		serverDelay = 0;
		specItem="";
		exchangePool="";
		resetTimeList="";
		refreshCost="";
		dailyExchangeTimes=0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public String getSpecItem() {
		return specItem;
	}

	public String getExchangePool() {
		return exchangePool;
	}

	public String getResetTimeList() {
		return resetTimeList;
	}

	public String getRefreshCost() {
		return refreshCost;
	}
	
	public List<RewardItem.Builder> getSpecItemAwardList() {
		return specItemAwardList;
	}

	public void setSpecItemAwardList(List<RewardItem.Builder> specItemAwardList) {
		this.specItemAwardList = specItemAwardList;
	}

	public List<RewardItem.Builder> getRefreshCostList() {
		return refreshCostList;
	}

	public void setRefreshCostList(List<RewardItem.Builder> refreshCostList) {
		this.refreshCostList = refreshCostList;
	}

	public int[] getResetTimeAry() {
		return resetTimeAry;
	}

	public void setResetTimeAry(int[] resetTimeAry) {
		this.resetTimeAry = resetTimeAry;
	}

	public int getDailyExchangeTimes() {
		return dailyExchangeTimes;
	}

	public int[][] getExchangePoolList() {
		return exchangePoolList;
	}

	public void setExchangePoolList(int[][] exchangePoolList) {
		this.exchangePoolList = exchangePoolList;
	}

	@Override
	public boolean assemble() {
		specItemAwardList = RewardHelper.toRewardItemImmutableList(specItem);
		refreshCostList = RewardHelper.toRewardItemImmutableList(refreshCost);
		resetTimeAry = SerializeHelper.string2IntArray(resetTimeList, "_");
		
		exchangePoolList = SerializeHelper.string2IntIntArray(exchangePool);
		
		return true;
	}
	
	@Override
	public boolean checkValid() {
		return ConfigChecker.getDefaultChecker().checkAwardsValid(specItem);		
	}
	
}
