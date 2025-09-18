package com.hawk.activity.type.impl.accumulateConsume.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 累计消费活动配置
 * 
 * @author Jesse
 *
 */
@HawkConfigManager.KVResource(file = "activity/accumulate_consume_achieve/consume_activity_cfg.xml")
public class AccumulateConsumeActivityKVCfg extends HawkConfigBase {
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;

	/**
	 * 是否跨天重置(配1则每天零点重置成就)
	 */
	private final int dailyRefresh;

	public AccumulateConsumeActivityKVCfg() {
		this.serverDelay = 0;
		this.dailyRefresh = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public boolean isDailyRefresh() {
		return dailyRefresh == 1;
	}
}
