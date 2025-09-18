package com.hawk.activity.type.impl.rechargeFund.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager.XmlResource;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

@XmlResource(file = "activity/recharge_fund/recharge_fund_level.xml")
public class RechargeFundLevelCfg extends HawkConfigBase {
	@Id
	private final int giftId;

	private final int unlockRecharge;

	private final String buyCost;

	private List<RewardItem.Builder> costList;

	public List<RewardItem.Builder> getCostList() {
		return costList;
	}

	public int getGiftId() {
		return giftId;
	}

	public int getUnlockRecharge() {
		return unlockRecharge;
	}

	public String getBuyCost() {
		return buyCost;
	}

	public RechargeFundLevelCfg() {
		this.giftId = 0;
		this.unlockRecharge = 0;
		this.buyCost = "";
	}

	@Override
	protected boolean assemble() {
		try {
			costList = RewardHelper.toRewardItemImmutableList(buyCost);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}

}
