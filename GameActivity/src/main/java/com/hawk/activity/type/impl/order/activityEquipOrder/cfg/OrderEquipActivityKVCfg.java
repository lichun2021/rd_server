package com.hawk.activity.type.impl.order.activityEquipOrder.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;


@HawkConfigManager.KVResource(file = "activity/equip_order/equip_order_cfg.xml")
public class OrderEquipActivityKVCfg extends HawkConfigBase {

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

	public OrderEquipActivityKVCfg() {
		serverDelay = 0;
		unlockGold = 0;
		unlockCostId = 0;
		unlockTime=0;
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

	@Override
	protected boolean assemble() {
		return true;
	}

}
