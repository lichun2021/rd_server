package com.hawk.activity.type.impl.urlModelSeven.cfg;

import com.hawk.activity.type.impl.urlReward.URLRewardBaseCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

/**
 * url模板活动7 KV配置
 * @author Jesse
 *
 */
@HawkConfigManager.KVResource(file = "activity/url_model_seven/url_model_seven_cfg.xml")
public class UrlModelSevenActivityKVCfg extends URLRewardBaseCfg {
	
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;
	
	// 开服时间晚于这个时间的服务器才开放
	private final String serverOpenTime;
	
	private long serverOpenLimitTime;

	public UrlModelSevenActivityKVCfg() {
		serverDelay = 0;
		serverOpenTime = "2023-9-25 00:00:00";
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}
	
	public boolean assemble() {
		serverOpenLimitTime = HawkTime.parseTime(serverOpenTime);
		return true;
	}
	
	public long getServerOpenLimitTime() {
		return serverOpenLimitTime;
	}

}
