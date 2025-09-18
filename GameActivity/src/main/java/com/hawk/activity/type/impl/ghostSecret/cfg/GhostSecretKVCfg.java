package com.hawk.activity.type.impl.ghostSecret.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "activity/equip_treasure/equip_treasure_cfg.xml")
public class GhostSecretKVCfg extends HawkConfigBase {
	
	//# 服务器开服延时开启活动时间；单位:秒
	private final int serverDelay;
	// 翻牌消耗
	private final String treasureCost;
	//触发多次特等奖时，只给1次；其他折算为本奖励内容
	private final String specRewardTransform;
	//每次翻牌的随机奖励
	private final int randReward;
	// 重置消耗
	private final String resetCost;
	//每日可手动重置次数
	private final int dailyResetTimes;
	//道具价格
	private final String itemOnecePrice;
	//购买1次获得固定奖励
	private final String extReward;
	
	public GhostSecretKVCfg(){
		serverDelay = 0;
		treasureCost = "";
		resetCost = "";
		specRewardTransform ="";
		randReward = 0;
		dailyResetTimes = 0;
		itemOnecePrice ="";
		extReward = "";
		
	}

	public long getServerDelay() {
		return ((long)serverDelay) * 1000;
	}

	public String getTreasureCost() {
		return treasureCost;
	}

	public String getSpecRewardTransform() {
		return specRewardTransform;
	}

	public int getRandReward() {
		return randReward;
	}

	public String getResetCost() {
		return resetCost;
	}

	public int getDailyResetTimes() {
		return dailyResetTimes;
	}

	public String getItemOnecePrice() {
		return itemOnecePrice;
	}

	public String getExtReward() {
		return extReward;
	}
	
}
