package com.hawk.activity.type.impl.giftzeronew.cfg;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

/**
 * 新0元礼包活动配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "activity/new_gift_zero/%s/new_gift_zero_reward.xml", autoLoad=false, loadParams="262")
public class GiftZeroNewRewardCfg extends HawkConfigBase {
	// 奖励ID
	@Id
	private final int id;
	// 奖励内容
	private final String award;
	// 购买礼包消耗内容
	private final String price;
	// 购买礼包消耗返还内容
	private final String consumeReturn;
	// 购买礼包消耗的返还期限
	private final int returnDays;
	
	/**
	 * 奖励
	 */
	private List<RewardItem.Builder> rewardList;
	/**
	 * 消耗
	 */
	private List<RewardItem.Builder> consumeItem;
	/**
	 * 消耗返还
	 */
	private Map<Integer, List<RewardItem.Builder>> consumeBackItemMap;

	public GiftZeroNewRewardCfg() {
		id = 0;
		award = "";
		price = "";
		consumeReturn = "";
		returnDays = 0;
	}
	
	public int getId() {
		return id;
	}

	public String getAward() {
		return award;
	}

	public String getPrice() {
		return price;
	}

	public String getReturnConsume() {
		return consumeReturn;
	}

	public int getReturnDays() {
		return returnDays;
	}
	
    public boolean assemble() {
		this.rewardList = RewardHelper.toRewardItemImmutableList(award);
		this.consumeItem = RewardHelper.toRewardItemImmutableList(price);
		
		String[] returnContents = consumeReturn.split(";");
		if (returnContents.length != returnDays) {
			return false;
		}
		
		consumeBackItemMap = new HashMap<Integer, List<RewardItem.Builder>>(); 
		for (int i = 0; i < returnDays; i++) {
			List<RewardItem.Builder> contentList = RewardHelper.toRewardItemImmutableList(returnContents[i]);
			consumeBackItemMap.put(i + 1, contentList);
		}
		
		return true;
	}
	
	@Override
	public boolean checkValid() {
		
		if(rewardList.isEmpty()) {
			throw new InvalidParameterException("GiftZeroNewRewardCfg reward is empty");
		}
		
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(award);
		if (!valid) {
			throw new InvalidParameterException(String.format("GiftZeroNewRewardCfg reward error, id: %s , item: %s", id, award));
		}
		
		if(consumeItem.isEmpty()) {
			throw new InvalidParameterException("GiftZeroNewRewardCfg consumeItem is empty");
		}
		
		if(consumeBackItemMap.isEmpty()) {
			throw new InvalidParameterException("GiftZeroNewRewardCfg consumeBackItem is empty");
		}
		
		return true;		
	}
	
	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}
	
	public List<RewardItem.Builder> getConsumeItem() {
		return consumeItem;
	}
	
	public List<RewardItem.Builder> getConsumeBackItem(int day) {
		 return consumeBackItemMap.get(day);
	}
	
}
