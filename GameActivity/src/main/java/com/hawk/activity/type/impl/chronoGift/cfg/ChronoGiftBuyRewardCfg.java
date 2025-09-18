package com.hawk.activity.type.impl.chronoGift.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 时空好礼时空之门直购礼包道具
 * 
 * @author che
 *
 */
@HawkConfigManager.XmlResource(file = "activity/space_gift/space_gift_reward.xml")
public class ChronoGiftBuyRewardCfg extends HawkConfigBase {
	// 奖励ID
	@Id
	private final int id;
	// 对应礼包ID
	private final int giftId;
	// 奖励内容
	private final String reward;

	public ChronoGiftBuyRewardCfg() {
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
