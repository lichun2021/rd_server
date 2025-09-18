package com.hawk.activity.type.impl.onermbpurchase.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;

import com.google.common.collect.ImmutableList;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.item.OddsItemInfo;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * 一元购活动配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "activity/one_rmb/one_rmb_reward.xml")
public class OneRMBPurchaseCfg extends HawkConfigBase {
	@Id /** 活动天数 */
	private final int id;
	/** 充值比得奖励 */
	private final String rewards;
	/** 充值随机奖励 */
	private final String randomRewards;
	
	private List<RewardItem.Builder> rewardList;
	private List<List<OddsItemInfo>> randomRewardList = new ArrayList<List<OddsItemInfo>>();
	private static Map<Integer, OneRMBPurchaseCfg> cfgMap = new HashMap<Integer, OneRMBPurchaseCfg>();
	// 奖励配置最后一天
	private static int lastDay = 0;

	public OneRMBPurchaseCfg() {
		id = 0;
		rewards = "";
		randomRewards = "";
	}
	
	@Override
	protected boolean assemble() {
		try {
			rewardList = RewardHelper.toRewardItemImmutableList(rewards);
			if (!HawkOSOperator.isEmptyString(randomRewards)) {
				String[] awardItemArray = randomRewards.split(";");
				for (String awardItemElem : awardItemArray) {
					String[] awardItems = awardItemElem.split(",");
					List<OddsItemInfo> itemInfos = new ArrayList<OddsItemInfo>(awardItems.length);
					for (String awardItem : awardItems) {
						OddsItemInfo itemInfo = OddsItemInfo.valueOf(awardItem);
						if (itemInfo != null) {
							itemInfos.add(itemInfo);
						}
					}

					randomRewardList.add(itemInfos);
				}
				randomRewardList = ImmutableList.copyOf(randomRewardList);
			}
			
			cfgMap.put(id, this);
			if (lastDay < id) {
				lastDay = id;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}

	public int getId() {
		return id;
	}

	public String getRewards() {
		return rewards;
	}

	public String getRandomRewards() {
		return randomRewards;
	}

	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}
	
	public static OneRMBPurchaseCfg getCfgByDay(int day) {
		if (day > lastDay) {
			return cfgMap.get(lastDay);
		}
		
		return cfgMap.get(day);
	}

	public List<RewardItem.Builder> getRandomRewardList() {
		List<RewardItem.Builder> rewardItemList = new ArrayList<RewardItem.Builder>();
		for (List<OddsItemInfo> items : randomRewardList) {
			List<Integer> weights = new ArrayList<Integer>();
			for (OddsItemInfo itemInfo : items) {
				weights.add(itemInfo.getProbability());
			}
			OddsItemInfo item = HawkRand.randomWeightObject(items, weights);
			RewardItem.Builder builder = RewardItem.newBuilder();
			builder.setItemType(item.getType());
			builder.setItemId(item.getItemId());
			builder.setItemCount(item.getCount());
			rewardItemList.add(builder);
		}
		
		return rewardItemList;
	}
	
	public Map<RewardItem.Builder, Integer> getRandomRewardMap() {
		Map<RewardItem.Builder, Integer> rewardItemList = new HashMap<RewardItem.Builder, Integer>();
		for (List<OddsItemInfo> items : randomRewardList) {
			List<Integer> weights = new ArrayList<Integer>();
			for (OddsItemInfo itemInfo : items) {
				weights.add(itemInfo.getProbability());
			}
			OddsItemInfo item = HawkRand.randomWeightObject(items, weights);
			RewardItem.Builder builder = RewardItem.newBuilder();
			builder.setItemType(item.getType());
			builder.setItemId(item.getItemId());
			builder.setItemCount(item.getCount());
			rewardItemList.put(builder, item.getRare());
		}
		
		return rewardItemList;
	}
	
}
