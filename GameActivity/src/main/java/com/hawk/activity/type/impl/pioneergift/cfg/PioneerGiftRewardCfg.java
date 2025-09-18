package com.hawk.activity.type.impl.pioneergift.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 先锋豪礼活动配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "activity/pioneer_gift/pioneer_gift_reward.xml")
public class PioneerGiftRewardCfg extends HawkConfigBase {
	// 礼包ID
	@Id
	private final int giftId;
	// 奖励内容
	private final String reward;

	public PioneerGiftRewardCfg() {
		giftId = 0;
		reward = "";
	}
	
	@Override
	protected boolean assemble() {
		return true;
	}

	public int getGiftId() {
		return giftId;
	}
	
	public String getReward() {
		return reward;
	}

}
