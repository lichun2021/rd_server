package com.hawk.activity.type.impl.urlModel342.cfg;

import com.hawk.activity.type.impl.urlReward.URLRewardBaseCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "activity/url_model_342/url_model_342_cfg.xml")
public class UrlModel342KVCfg extends URLRewardBaseCfg {

	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;

	private final String serverOpenTime;
	private final String serverEndOpenTime;
	
	public UrlModel342KVCfg() {
		serverDelay = 0;
		serverOpenTime = "";
		serverEndOpenTime = "";
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public String getServerOpenTime() {
		return serverOpenTime;
	}

	public String getServerEndOpenTime() {
		return serverEndOpenTime;
	}
}
