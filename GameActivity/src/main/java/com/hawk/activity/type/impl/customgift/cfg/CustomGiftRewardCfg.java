package com.hawk.activity.type.impl.customgift.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 定制礼包活动配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "activity/custom_made/custom_made_reward.xml")
public class CustomGiftRewardCfg extends HawkConfigBase {
	// 奖励ID
	@Id
	private final int id;
	// 对应礼包ID
	private final int giftId;
	// 奖励内容
	private final String reward;

	public CustomGiftRewardCfg() {
		id = 0;
		giftId = 0;
		reward = "";
	}
	
	@Override
	protected boolean assemble() {
		return true;
	}

	public int getId() {
		return id;
	}

	public int getGiftId() {
		return giftId;
	}

	public String getReward() {
		return reward;
	}

}
