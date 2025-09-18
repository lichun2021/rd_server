package com.hawk.activity.type.impl.festival.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 八日盛典活动配置
 * @author PhilChen
 *
 */
@HawkConfigManager.KVResource(file = "activity/festival/%s/festival_activity_cfg.xml", autoLoad=false, loadParams="10")
public class FestivalActivityKVCfg extends HawkConfigBase {

	private final int receiveRewardLimitDay;

	public FestivalActivityKVCfg() {
		receiveRewardLimitDay = 0;
	}

	public int getReceiveRewardLimitDay() {
		return receiveRewardLimitDay;
	}
}
