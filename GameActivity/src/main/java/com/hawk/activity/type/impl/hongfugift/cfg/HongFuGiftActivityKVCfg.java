package com.hawk.activity.type.impl.hongfugift.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 洪福礼包活动配置
 * @author hf
 */
@HawkConfigManager.KVResource(file = "activity/blessing_gift/blessing_gift_cfg.xml")
public class HongFuGiftActivityKVCfg extends HawkConfigBase {
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;
	/**
	 * 活动奖励领取天数
	 */
	private final int activityDay;


	public HongFuGiftActivityKVCfg() {
		serverDelay = 0;
		activityDay = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public int getActivityDay() {
		return activityDay;
	}
}
