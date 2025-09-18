package com.hawk.activity.type.impl.roulette.cfg;

import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager.KVResource;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

@KVResource(file = "activity/roulette/roulette_activity_cfg.xml")
public class RouletteKVCfg extends HawkConfigBase {
	private final int serverDelay;

	/**
	 * 每次幸运值_总幸运值
	 */
	private final int luckyValue;

	// 时空宝箱奖励
	private final String timeChestReward;

	/**
	 * 获取奖励
	 */
	private final String extReward;

	/**
	 * 一个道具.
	 */

	private final String itemOnce;
	/**
	 * 一个道具的价格
	 */
	private final String itemOnecePrice;

	// 每日抽奖次数限制
	private final int maxTimes;
	
	private final boolean freeTimes;

	private List<RewardItem.Builder> itemOneList;
	private List<RewardItem.Builder> itemOnecePriceList;
	private List<RewardItem.Builder> timeChestRewardList;
	/**
	 * 额外奖励
	 */
	private List<RewardItem.Builder> extRewardList;

	public RouletteKVCfg() {
		this.serverDelay = 0;
		this.luckyValue = 0;
		this.timeChestReward = "";
		this.extReward = "";
		this.itemOnce = "";
		this.itemOnecePrice = "";
		this.maxTimes = 0;
		freeTimes = true;
	}

	@Override
	public boolean assemble() {
		try {
			this.itemOnecePriceList = RewardHelper.toRewardItemImmutableList(itemOnecePrice);
			this.itemOneList = RewardHelper.toRewardItemImmutableList(itemOnce);
			this.extRewardList = RewardHelper.toRewardItemImmutableList(extReward);
			this.timeChestRewardList = RewardHelper.toRewardItemImmutableList(timeChestReward);
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
	}

	/**
	 * 抽奖扣除的物品ID
	 * 
	 * @return
	 */
	public int getCostItemId() {
		return itemOneList.get(0).getItemId();
	}

	/**
	 * 抽奖一次扣除的物品个数
	 * 
	 * @return
	 */
	public int getCostOneNum() {
		return (int) itemOneList.get(0).getItemCount();
	}

	@Override
	public boolean checkValid() {
		if (itemOnecePriceList.size() <= 0 || itemOnecePriceList.size() > 1) {
			throw new InvalidParameterException("时空轮盘单次物品消耗三段式只能配置一个物品,配置多个物品与逻辑不符");
		}
		return true;
	}

	public String getItemOnce() {
		return itemOnce;
	}

	public String getItemOnecePrice() {
		return itemOnecePrice;
	}

	public int getLuckyValue() {
		return this.luckyValue;
	}

	public List<RewardItem.Builder> getItemOneList() {
		return itemOneList;
	}

	public List<RewardItem.Builder> getItemOnecePriceList() {
		return itemOnecePriceList;
	}
	
	public List<RewardItem.Builder> getTimeChestRewardList(){
		return this.timeChestRewardList;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public List<RewardItem.Builder> getExtRewardList() {
		return Collections.unmodifiableList(extRewardList);
	}

	public int getMaxTimes() {
		return maxTimes;
	}

	public boolean isFreeTimes() {
		return freeTimes;
	}
}
