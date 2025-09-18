package com.hawk.activity.type.impl.samuraiBlackened.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 黑武士
 * @author jm
 *
 */
@HawkConfigManager.KVResource(file = "activity/samurai_blackened/samurai_blackened_cfg.xml")
public class SamuraiBlackenedActivityKVCfg extends HawkConfigBase {

	private final int receiveRewardLimitDay;
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;


	public SamuraiBlackenedActivityKVCfg() {
		serverDelay = 0;
		receiveRewardLimitDay = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}
	
	public int getReceiveRewardLimitDay() {
		return receiveRewardLimitDay;
	}
}
