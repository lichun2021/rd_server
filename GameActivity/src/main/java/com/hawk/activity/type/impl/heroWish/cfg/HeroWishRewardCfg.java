package com.hawk.activity.type.impl.heroWish.cfg;

import java.security.InvalidParameterException;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

@HawkConfigManager.XmlResource(file = "activity/hero_wish/hero_wish_reward.xml")
public class HeroWishRewardCfg extends HawkConfigBase {
	/**
	 * 唯一ID
	 */
	@Id
	private final int id;

	/**
	 * 兑换需要的物品
	 */
	private final String rewardItem;
	
	/**
	 * 选择增加
	 */
	private final int chooseAdd;

	/**
	 * 许愿增加数量
	 */
	private final int wishAdd;

	



	public HeroWishRewardCfg() {
		id = 0;
		rewardItem = "";
		wishAdd = 0;
		chooseAdd= 0;
	}

	public int getId() {
		return id;
	}

	
	public int getWishAdd() {
		return wishAdd;
	}
	
	public int getChooseAdd() {
		return chooseAdd;
	}
	

	
	public List<RewardItem.Builder> getRewardItemList() {
		return RewardHelper.toRewardItemImmutableList(this.rewardItem);
	}
	
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(rewardItem);
		if (!valid) {
			throw new InvalidParameterException(String.format("AllianceWishExchangeCfg needItem error, id: %s , needItem: %s", id, rewardItem));
		}
		return super.checkValid();
	}

}
