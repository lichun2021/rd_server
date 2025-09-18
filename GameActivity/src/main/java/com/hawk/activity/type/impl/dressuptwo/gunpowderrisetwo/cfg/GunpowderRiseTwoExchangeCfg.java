package com.hawk.activity.type.impl.dressuptwo.gunpowderrisetwo.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import java.security.InvalidParameterException;
import java.util.List;

/**
 * 圣诞节系列活动三:冰雪商城活动
 */
@HawkConfigManager.XmlResource(file = "activity/christmas_snow_shop/christmas_snow_shop_exchange.xml")
public class GunpowderRiseTwoExchangeCfg extends HawkConfigBase {
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
	 * 每天的兑换次数
	 */
	private final int exchangeCount;

	private List<RewardItem.Builder> gainItemList;

	private List<RewardItem.Builder> payItemList;

	public GunpowderRiseTwoExchangeCfg() {
		id = 0;
		pay = "";
		gain = "";
		exchangeCount = 0;
	}

	public int getId() {
		return id;
	}


	public int getExchangeCount() {
		return exchangeCount;
	}

	public boolean assemble() {
		try {
			this.gainItemList = RewardHelper.toRewardItemImmutableList(this.gain);
			this.payItemList = RewardHelper.toRewardItemImmutableList(this.pay);
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
			throw new InvalidParameterException(String.format("GunpowderRiseTwoExchangeCfg reward error, id: %s , needItem: %s", id, pay));
		}
		valid = ConfigChecker.getDefaultChecker().checkAwardsValid(gain);
		if (!valid) {
			throw new InvalidParameterException(String.format("GunpowderRiseTwoExchangeCfg reward error, id: %s , gainItem: %s", id, gain));
		}
		return super.checkValid();
	}

	public String getPay() {
		return pay;
	}

	public String getGain() {
		return gain;
	}

	public List<RewardItem.Builder> getGainItemList() {
		return gainItemList;
	}

	public void setGainItemList(List<RewardItem.Builder> gainItemList) {
		this.gainItemList = gainItemList;
	}

	public List<RewardItem.Builder> getPayItemList() {
		return payItemList;
	}

	public void setPayItemList(List<RewardItem.Builder> payItemList) {
		this.payItemList = payItemList;
	}
}
