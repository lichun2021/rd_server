package com.hawk.activity.type.impl.midAutumn.cfg;

import java.security.InvalidParameterException;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

/**中秋兑换
 * @author Winder
 */
@HawkConfigManager.XmlResource(file = "activity/mid_autumn/mid_autumn_exchange.xml")
public class MidAutumnExchangeCfg extends HawkConfigBase {
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
	 * 每天的兑换次数
	 */
	private final int maxTime;

	private List<RewardItem.Builder> gainItemList;

	private List<RewardItem.Builder> needItemList;

	public MidAutumnExchangeCfg() {
		id = 0;
		needItem = "";
		gainItem = "";
		maxTime = 0;
	}

	public int getId() {
		return id;
	}

	public String getNeedItem() {
		return needItem;
	}

	public String getGainItem() {
		return gainItem;
	}

	public boolean assemble() {
		try {
			this.gainItemList = RewardHelper.toRewardItemImmutableList(this.gainItem);
			this.needItemList = RewardHelper.toRewardItemImmutableList(this.needItem);
			return true;
		} catch (Exception arg1) {
			HawkException.catchException(arg1, new Object[0]);
			return false;
		}
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(needItem);
		if (!valid) {
			throw new InvalidParameterException(String.format("ActivityExchangeCfg reward error, id: %s , needItem: %s", id, needItem));
		}
		valid = ConfigChecker.getDefaultChecker().checkAwardsValid(gainItem);
		if (!valid) {
			throw new InvalidParameterException(String.format("ActivityExchangeCfg reward error, id: %s , gainItem: %s", id, gainItem));
		}
		return super.checkValid();
	}

	public List<RewardItem.Builder> getNeedItemList() {
		return needItemList;
	}

	public List<RewardItem.Builder> getGainItemList() {
		return gainItemList;
	}

	public int getMaxTime() {
		return maxTime;
	}
}
