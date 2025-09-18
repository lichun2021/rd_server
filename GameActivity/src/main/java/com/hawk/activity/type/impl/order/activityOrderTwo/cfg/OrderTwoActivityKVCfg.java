package com.hawk.activity.type.impl.order.activityOrderTwo.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;


@HawkConfigManager.KVResource(file = "activity/new_order/new_order_cfg.xml")
public class OrderTwoActivityKVCfg extends HawkConfigBase {

	/** 服务器开服延时开启活动时间 单位:s */
	private final int serverDelay;

	/**
	 * 解锁金条公式（经验*0.01金条）
	 */
	private final int unlockGold;

	/**
	 * 买exp货币单位
	 */
	private final int unlockCostId;
	
	private final int unlockTime;
	
	private final int doubleProbability;
	
	private final String version20240905Time;

	public OrderTwoActivityKVCfg() {
		serverDelay = 0;
		unlockGold = 0;
		unlockCostId = 0;
		unlockTime=0;
		doubleProbability = 0;
		version20240905Time = "2024-9-11 12:00:00";
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}
	
	public int getUnlockGold() {
		return unlockGold;
	}

	public int getUnlockCostId() {
		return unlockCostId;
	}

	public int getUnlockTime() {
		return unlockTime;
	}
	
	public int getDoubleProbability() {
		return doubleProbability;
	}
	
	public String getVersion20240905Time() {
		return version20240905Time;
	}

	@Override
	protected boolean assemble() {
		return true;
	}

}
