package com.hawk.activity.type.impl.dressuptwo.firereignitetwo.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import java.security.InvalidParameterException;
import java.util.List;

/**
 * 圣诞节系列活动二:冬日装扮活动
 */
@HawkConfigManager.XmlResource(file = "activity/christmas_winter_dress/christmas_winter_dress_exchange.xml")
public class FireReigniteTwoExchangeCfg extends HawkConfigBase {
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


	public FireReigniteTwoExchangeCfg() {
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
