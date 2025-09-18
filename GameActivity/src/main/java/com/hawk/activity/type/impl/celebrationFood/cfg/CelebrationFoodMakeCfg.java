package com.hawk.activity.type.impl.celebrationFood.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 庆典美食
 */
@HawkConfigManager.XmlResource(file = "activity/celebration_share_cake/celebration_share_cake_achieve.xml")
public class CelebrationFoodMakeCfg extends HawkConfigBase{
	//等级
	@Id
	private final int lv;
	//制作消耗
	private final String cakeCost; 
	//普通奖励
	private final String rewards;
	// 25元进阶奖励
	private final String bestRewards;
	// 50元进阶奖励
	private final String supreRewards;
	
	public CelebrationFoodMakeCfg() {
		lv = 0;
		cakeCost = "";
		rewards = "";
		bestRewards = "";
		supreRewards = "";
	}

	public int getLv() {
		return lv;
	}

	public String getCakeCost() {
		return cakeCost;
	}

	public String getRewards() {
		return rewards;
	}

	public String getBestRewards() {
		return bestRewards;
	}

	public String getSupreRewards() {
		return supreRewards;
	}
	
}
