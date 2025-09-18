package com.hawk.activity.type.impl.starInvest.cfg;

import java.security.InvalidParameterException;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;


@HawkConfigManager.XmlResource(file = "activity/star_invest/star_invest_gift.xml")
public class StarInvestGiftCfg extends HawkConfigBase {
	// 礼包ID
	@Id 
	private final int id;
	private final String reward;
	
	public StarInvestGiftCfg() {
		id = 0;
		reward = "";
	}
	
	@Override
	protected boolean assemble() {
		return true;
	}


	public List<RewardItem.Builder> getRewardList() {
		return  RewardHelper.toRewardItemImmutableList(this.reward);
	}

	public int getId() {
		return id;
	}


	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(this.reward);
		if (!valid) {
			throw new InvalidParameterException(String.format("StarInvestGiftCfg reward error, id: %s , reward: %s", id, reward));
		}
		return super.checkValid();
	}
}
