package com.hawk.activity.type.impl.pandoraBox.cfg;

import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager.KVResource;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

@KVResource(file="activity/pandora_box/pandora_box_activity_cfg.xml")
public class PandoraBoxConfig extends HawkConfigBase {
	private  final int serverDelay;
	/**
	 * 一个道具.
	 */
	private final String itemOnce;
	/**
	 * 一个道具的价格
	 */
	private final String itemOnecePrice;
	/**
	 * 一次积分
	 */
	private final int scoreOnce;
	/**
	 * 十次积分
	 */
	private final int scoreTen;
	
	/** 是否免费开启一次 1为每天重置一次，提供免费，0为不可免费开启 **/
	private final int free;
	
	/** 商城道具是否需要重置 1为每日重置，0为不重置 **/
	private final int reset;
	/**
	 * 每日的最大抽奖次数
	 */
	private final int maxDailyLotteryTimes;
	
	/**
	 * 获取奖励
	 */
	private final String extReward;
	
	private List<RewardItem.Builder> itemOneList;
	private List<RewardItem.Builder> itemOnecePriceList;
	/**
	 * 额外奖励
	 */
	private List<RewardItem.Builder> extRewardList;
	
	private static PandoraBoxConfig instance = null;
	public static PandoraBoxConfig getInstance() {
		return instance;
	}
	public PandoraBoxConfig() {
		this.itemOnce = "";
		this.itemOnecePrice = "";
		this.scoreOnce = 0;
		this.scoreTen = 0;
		this.free = 0;
		this.reset = 0;
		this.serverDelay = 0;
		this.maxDailyLotteryTimes = 0;
		this.extReward = "";
		instance = this;
	}
	public int getFree() {
		return free;
	}
	public int getReset() {
		return reset;
	}
	
	public boolean freeOnce() {
		return this.free > 0;
	}
	public boolean storeReset() {
		return this.reset > 0;
	}
	
	@Override
	public boolean assemble() {
		this.itemOnecePriceList = RewardHelper.toRewardItemImmutableList(itemOnecePrice);
		this.itemOneList = RewardHelper.toRewardItemImmutableList(itemOnce);
		this.extRewardList = RewardHelper.toRewardItemImmutableList(extReward);
		
		return true;
	}
	/**
	 * 抽奖扣除的物品ID
	 * @return
	 */
	public int getCostItemId() {
		return itemOneList.get(0).getItemId();
	}
	/**
	 * 抽奖一次扣除的物品个数
	 * @return
	 */
	public int getCostOneNum() {
		return (int)itemOneList.get(0).getItemCount();
	}
	@Override
	public boolean checkValid() {
		//RewardHelper.checkRewardItem(itemOneList);
		//RewardHelper.checkRewardItem(itemOnecePriceList);
		if (itemOnecePriceList.size() <= 0 || itemOnecePriceList.size() > 1) {
			throw new InvalidParameterException("潘多拉魔盒单次物品消耗三段式只能配置一个物品,配置多个物品与逻辑不符");
		}
		return true;
	}
	public String getItemOnce() {
		return itemOnce;
	}
	public String getItemOnecePrice() {
		return itemOnecePrice;
	}
	public int getScoreOnce() {
		return scoreOnce;
	}
	public int getScoreTen() {
		return scoreTen;
	}
	public List<RewardItem.Builder> getItemOneList() {
		return itemOneList;
	}
	public List<RewardItem.Builder> getItemOnecePriceList() {
		return itemOnecePriceList;
	}
	public long getServerDelay() {
		return serverDelay * 1000l;
	}
	public int getMaxDailyLotteryTimes() {
		return maxDailyLotteryTimes;
	}
	public List<RewardItem.Builder> getExtRewardList() {
		return Collections.unmodifiableList(extRewardList);
	}
}
