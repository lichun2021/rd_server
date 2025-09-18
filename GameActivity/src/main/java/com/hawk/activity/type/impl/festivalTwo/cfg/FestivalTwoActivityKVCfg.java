package com.hawk.activity.type.impl.festivalTwo.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 八日盛典活动配置
 * @author PhilChen
 *
 */
@HawkConfigManager.KVResource(file = "activity/festival_two/festival_two_activity_cfg.xml")
public class FestivalTwoActivityKVCfg extends HawkConfigBase {

	private final int receiveRewardLimitDay;
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;


	public FestivalTwoActivityKVCfg() {
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
