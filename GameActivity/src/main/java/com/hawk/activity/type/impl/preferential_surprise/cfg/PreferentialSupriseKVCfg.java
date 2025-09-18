package com.hawk.activity.type.impl.preferential_surprise.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 特惠惊喜活动K-V配置
 * @author Jesse
 *
 */
@HawkConfigManager.KVResource(file = "activity/preferential_surprise/preferential_surprise_cfg.xml")
public class PreferentialSupriseKVCfg extends HawkConfigBase {
	
	/** 服务器开服延时开启活动时间*/
	private final int serverDelay;
	
	/** 是否每日重置(宝箱除外)*/
	private final int isDailyReset;
	
	public PreferentialSupriseKVCfg() {
		serverDelay = 0;
		isDailyReset = 0;
	}
	
	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public boolean isDailyReset() {
		return isDailyReset == 1;
	}
}