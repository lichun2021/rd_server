package com.hawk.activity.type.impl.giftzero.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 0元礼包活动配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.KVResource(file = "activity/gift_zero/gift_zero_cfg.xml")
public class GiftZeroActivityKVCfg extends HawkConfigBase {

	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;
	/**
	 * 活动开启时长(自开服后)
	 */
	private final int activityOpenDuration;
	/**
	 * 活动结束后展示时长
	 */
	private final int activityShowDuration;

	public GiftZeroActivityKVCfg() {
		serverDelay = 0;
        activityOpenDuration = 1209600;
        activityShowDuration = 432000;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public int getActivityOpenDuration() {
		return activityOpenDuration;
	}

	public int getActivityShowDuration() {
		return activityShowDuration;
	}
	
}
