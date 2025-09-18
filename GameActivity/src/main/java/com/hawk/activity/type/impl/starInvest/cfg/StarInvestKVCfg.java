package com.hawk.activity.type.impl.starInvest.cfg;

import java.security.InvalidParameterException;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

@HawkConfigManager.KVResource(file = "activity/star_invest/star_invest_cfg.xml")
public class StarInvestKVCfg extends HawkConfigBase {
	
	//# 服务器开服延时开启活动时间；单位:秒
	private final int serverDelay;
	

	private final String speedItem;

	private final int speedTime;

	private final String speedItemBuyCost;

	private final int upLimit;
	
	private final int count;
	
	public StarInvestKVCfg(){
		serverDelay = 0;
		speedItem = "";
		speedTime = 0;
		speedItemBuyCost = "";
		upLimit = 0;
		count = 0;
	}

	public long getServerDelay() {
		return ((long)serverDelay) * 1000;
	}
	
	public RewardItem.Builder getSpeedItem() {
		return RewardHelper.toRewardItem(this.speedItem);
	}
	
	public int getSpeedTime() {
		return speedTime;
	}
	
	public List<RewardItem.Builder> getSpeedItemBuyCostList() {
		return RewardHelper.toRewardItemImmutableList(this.speedItemBuyCost);
	}

	public int getUpLimit() {
		return upLimit;
	}
	
	public int getCount() {
		return count;
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(this.speedItem);
		if (!valid) {
			throw new InvalidParameterException(String.format("StarInvestKVCfg reward error, id: %s , reward: %s", 1, speedItem));
		}
		valid = ConfigChecker.getDefaultChecker().checkAwardsValid(this.speedItemBuyCost);
		if (!valid) {
			throw new InvalidParameterException(String.format("StarInvestKVCfg reward error, id: %s , reward: %s", 1, speedItemBuyCost));
		}
		return super.checkValid();
	}
}
