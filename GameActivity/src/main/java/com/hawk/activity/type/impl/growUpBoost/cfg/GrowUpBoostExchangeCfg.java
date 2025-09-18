package com.hawk.activity.type.impl.growUpBoost.cfg;

import java.security.InvalidParameterException;
import java.util.List;

import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.impl.exchangeTip.AExchangeTipConfig;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

/**
 *  中部养成计划
 * 
 * @author che
 *
 */
@HawkConfigManager.XmlResource(file = "activity/grow_up_boost/grow_up_boost_exchange.xml")
public class GrowUpBoostExchangeCfg extends AExchangeTipConfig {
	// 奖励ID
	@Id
	private final int id;
	private final int configId;
	private final int group;
	private final String needItem;
	private final String gainItem;
	private final int times;

	public GrowUpBoostExchangeCfg() {
		id = 0;
		configId = 0;
		group = 0;
		needItem = "";
		gainItem = "";
		times = 0;
	}
	
	@Override
	protected boolean assemble() {
		return true;
	}

	@Override
	public int getId() {
		return id;
	}

	public int getConfigId() {
		return configId;
	}

	public int getGroup() {
		return group;
	}

	public String getNeedItem() {
		return needItem;
	}

	public String getGainItem() {
		return gainItem;
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
			throw new InvalidParameterException(String.format("GrowUpBoostExchangeCfg reward error, id: %s , needItem: %s", id, needItem));
		}
		valid = ConfigChecker.getDefaultChecker().checkAwardsValid(gainItem);
		if (!valid) {
			throw new InvalidParameterException(String.format("GrowUpBoostExchangeCfg reward error, id: %s , gainItem: %s", id, gainItem));
		}
		return super.checkValid();
	}
	

}
