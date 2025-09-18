package com.hawk.activity.type.impl.dressuptwo.christmasrecharge.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 圣诞节系列活动 累积充值
 * @author hf
 *
 */
@HawkConfigManager.KVResource(file = "activity/christmas_recharge/christmas_recharge_activity_cfg.xml")
public class ChristmasRechargeActivityKVCfg extends HawkConfigBase {
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;

	/**
	 * 是否跨天重置(配1则每天零点重置成就)
	 */
	private final int dailyRefresh;

	public ChristmasRechargeActivityKVCfg() {
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
