package com.hawk.activity.type.impl.hongfugift.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;


/**
 * 洪福礼包活动奖励配置
 * @author hf
 */
@HawkConfigManager.XmlResource(file = "activity/blessing_gift/blessing_gift.xml")
public class HongFuGiftCfg extends HawkConfigBase {
	@Id
	private final int giftId;
	private final String  androidPayId;
	private final String  iosPayId;
	private final int isFree;
	private final String  fixedReward;
	private final String  unlockReward;


	public HongFuGiftCfg() {
		giftId = 0;
		androidPayId = "";
		iosPayId = "";
		isFree = 0;
		fixedReward = "";
		unlockReward = "";
	}

	public int getGiftId() {
		return giftId;
	}

	public String getAndroidPayId() {
		return androidPayId;
	}

	public String getIosPayId() {
		return iosPayId;
	}

	public int getIsFree() {
		return isFree;
	}

	/**
	 * 是否免费
	 * @return
	 */
	public boolean isFree() {
		return isFree == 1;
	}

	public String getFixedReward() {
		return fixedReward;
	}

	public String getUnlockReward() {
		return unlockReward;
	}
}
