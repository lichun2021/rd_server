package com.hawk.activity.type.impl.urlModel344.cfg;

import com.hawk.activity.type.impl.urlReward.URLRewardBaseCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "activity/url_model_344/url_model_344_cfg.xml")
public class UrlModel344KVCfg extends URLRewardBaseCfg {

	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;

	private final String serverOpenTime;
	private final String serverEndTime;
	
	public UrlModel344KVCfg() {
		serverDelay = 0;
		serverOpenTime = "";
		serverEndTime = "";
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public String getServerOpenTime() {
		return serverOpenTime;
	}

	public String getServerEndTime() {
		return serverEndTime;
	}
	
}
