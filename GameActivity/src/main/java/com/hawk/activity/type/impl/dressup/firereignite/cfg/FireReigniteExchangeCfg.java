package com.hawk.activity.type.impl.dressup.firereignite.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import java.security.InvalidParameterException;
import java.util.List;

@HawkConfigManager.XmlResource(file = "activity/dress_war_reburning/dress_war_reburning_exchange.xml")
public class FireReigniteExchangeCfg extends HawkConfigBase {
	/**
	 * 唯一ID
	 */
	@Id
	private final int id;

	/**
	 * 兑换需要的物品
	 */
	private final String pay;

	/**
	 * 兑换获得物品
	 */
	private final String gain;

	/**
	 * 兑换给的经验
	 */
	private final int expGain;

	private List<RewardItem.Builder> payItemList;

	private List<RewardItem.Builder> gainItemList;

	public FireReigniteExchangeCfg() {
		id = 0;
		pay = "";
		gain = "";
		expGain = 0;
	}

	public int getId() {
		return id;
	}

	public String getPay() {
		return pay;
	}

	public String getGain() {
		return gain;
	}

	public int getExpGain() {
		return expGain;
	}

	public List<RewardItem.Builder> getPayItemList() {
		return payItemList;
	}

	public void setPayItemList(List<RewardItem.Builder> payItemList) {
		this.payItemList = payItemList;
	}

	public void setGainItemList(List<RewardItem.Builder> gainItemList) {
		this.gainItemList = gainItemList;
	}

	public List<RewardItem.Builder> getGainItemList() {
		return gainItemList;
	}

	public boolean assemble() {
		try {
			this.payItemList = RewardHelper.toRewardItemImmutableList(this.pay);
			this.gainItemList = RewardHelper.toRewardItemImmutableList(this.gain);
			return true;
		} catch (Exception arg1) {
			HawkException.catchException(arg1, new Object[0]);
			return false;
		}
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(pay);
		if (!valid) {
			throw new InvalidParameterException(String.format("ActivityExchangeCfg reward error, id: %s , needItem: %s", id, pay));
		}
		valid = ConfigChecker.getDefaultChecker().checkAwardsValid(gain);
		if (!valid) {
			throw new InvalidParameterException(String.format("ActivityExchangeCfg reward error, id: %s , gainItem: %s", id, gain));
		}
		return super.checkValid();
	}

}
