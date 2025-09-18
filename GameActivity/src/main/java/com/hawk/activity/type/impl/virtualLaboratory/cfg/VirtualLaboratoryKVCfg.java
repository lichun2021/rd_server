package com.hawk.activity.type.impl.virtualLaboratory.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "activity/armed_science/armed_science_cfg.xml")
public class VirtualLaboratoryKVCfg extends HawkConfigBase {
	
	//# 服务器开服延时开启活动时间；单位:秒
	private final int serverDelay;
	// 重置消耗
	private final String singlePrice;
	//道具价格
	private final String itemPrice;
	//购买1次获得固定奖励
	private final String extReward;
	//每日可手动重置次数
	private final int dailyResetTimes;
	//每次翻牌的随机奖励
	private final int randReward;

	
	public VirtualLaboratoryKVCfg(){
		serverDelay = 0;
		singlePrice = "";
		itemPrice = "";
		extReward = "";
		randReward = 0;
		dailyResetTimes = 0;
	}

	public long getServerDelay() {
		return ((long)serverDelay) * 1000;
	}

	public String getSinglePrice() {
		return singlePrice;
	}

	public String getItemPrice() {
		return itemPrice;
	}

	public String getExtReward() {
		return extReward;
	}

	public int getDailyResetTimes() {
		return dailyResetTimes;
	}

	public int getRandReward() {
		return randReward;
	}
	
}
