package com.hawk.activity.type.impl.hongfugift.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 洪福礼包活动
 * @author hf
 */
@HawkConfigManager.XmlResource(file = "activity/blessing_gift/blessing_gift_choose.xml")
public class HongFuRewardChooseCfg extends HawkConfigBase {
	@Id
	private final int id;

	private final int giftId;

	private final String reward;

	public HongFuRewardChooseCfg() {
		id = 0;
		giftId = 0;
		reward = "";
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
