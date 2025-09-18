package com.hawk.activity.type.impl.redrecharge.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "activity/red_recharge/red_recharge_activity_cfg.xml")
public class HappyRedRechargeKVConfig extends HawkConfigBase {
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;
	/**
	 * 是否累加，1代表累加，0代表不累加（活动最后一日，可购买次数=活动总天数-已购买次数）
	 */
	private final int isSummation;
	/**
	 * 每个礼包每日限购次数
	 */
	private final int purchasesTimes;

	public HappyRedRechargeKVConfig() {
		serverDelay = 0;
		isSummation = 0;
		purchasesTimes = 1;
	}

	public long getServerDelay() {
		return serverDelay * 1000L;
	}

	public int getIsSummation() {
		return isSummation;
	}

	public boolean isSummation() {
		return isSummation == 1;
	}
	
	public int getLimit() {
		return purchasesTimes;
	}
	
}
