package com.hawk.activity.type.impl.giftzero.cfg;

import java.security.InvalidParameterException;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

/**
 * 0元礼包活动配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "activity/gift_zero/gift_zero_reward.xml")
public class GiftZeroRewardCfg extends HawkConfigBase {
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
	private List<RewardItem.Builder> consumeBackItem;

	public GiftZeroRewardCfg() {
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
		this.consumeBackItem = RewardHelper.toRewardItemImmutableList(consumeReturn);
		return true;
	}
	
	@Override
	public boolean checkValid() {
		
		if(rewardList.isEmpty()) {
			throw new InvalidParameterException("GiftZeroRewardCfg reward is empty");
		}
		
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(award);
		if (!valid) {
			throw new InvalidParameterException(String.format("GiftZeroRewardCfg reward error, id: %s , item: %s", id, award));
		}
		
		if(consumeItem.isEmpty()) {
			throw new InvalidParameterException("GiftZeroRewardCfg consumeItem is empty");
		}
		
		if(consumeBackItem.isEmpty()) {
			throw new InvalidParameterException("GiftZeroRewardCfg consumeBackItem is empty");
		}
		
		return true;		
	}
	
	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}
	
	public List<RewardItem.Builder> getConsumeItem() {
		return consumeItem;
	}
	
	public List<RewardItem.Builder> getConsumeBackItem() {
		 return consumeBackItem;
	}
	
}
