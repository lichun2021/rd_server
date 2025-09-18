package com.hawk.activity.type.impl.machineLab.cfg;

import java.security.InvalidParameterException;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

@HawkConfigManager.XmlResource(file = "activity/machine_lab/machine_lab_exchange.xml")
public class MachineLabExchangeCfg extends HawkConfigBase {
	/**
	 * 唯一ID
	 */
	@Id
	private final int id;

	/**
	 * 兑换需要的物品
	 */
	private final String needItem;
	/**
	 * 兑换获得物品
	 */
	private final String gainItem;

	/**
	 * 最大数量
	 */
	private final int times;
	



	public MachineLabExchangeCfg() {
		id = 0;
		needItem = "";
		gainItem = "";
		times = 0;
	}

	public int getId() {
		return id;
	}

	
	
	
	
	public int getTimes() {
		return times;
	}

	
	public List<RewardItem.Builder> getNeedItemList() {
		return RewardHelper.toRewardItemImmutableList(this.needItem);
	}
	
	public List<RewardItem.Builder> getGainItemList() {
		return RewardHelper.toRewardItemImmutableList(this.gainItem);
	}
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(needItem);
		if (!valid) {
			throw new InvalidParameterException(String.format("AllianceWishExchangeCfg needItem error, id: %s , needItem: %s", id, needItem));
		}
		valid = ConfigChecker.getDefaultChecker().checkAwardsValid(gainItem);
		if (!valid) {
			throw new InvalidParameterException(String.format("AllianceWishExchangeCfg gainItem error, id: %s , gainItem: %s", id, gainItem));
		}
		return super.checkValid();
	}

}
