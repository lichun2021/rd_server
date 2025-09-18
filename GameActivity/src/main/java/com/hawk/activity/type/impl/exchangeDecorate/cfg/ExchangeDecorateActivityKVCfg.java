package com.hawk.activity.type.impl.exchangeDecorate.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "activity/freeavatar/freeavatar_cfg.xml")
public class ExchangeDecorateActivityKVCfg extends HawkConfigBase {
	//服务器开服延时开启活动时间；单位：秒
	private final int serverDelay;
	// 解锁金条公式（经验*0.01金条）
	private final int unlockGold;
	
	//解锁等级
	private final int unlockLevel;
	
	/**
	 * 每周购买次数
	 */
	private final int unlockTime;
	
	/**
	 * 买exp货币单位
	 */
	private final int unlockCostId;

	public ExchangeDecorateActivityKVCfg() {
		serverDelay = 0;
		unlockGold  = 0;
		unlockLevel = 0;
		unlockTime = 0;
		unlockCostId=0;
	}

	public int getUnlockGold() {
		return unlockGold;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public int getUnlockLevel() {
		return unlockLevel;
	}

	public int getUnlockTime() {
		return unlockTime;
	}

	public int getUnlockCostId() {
		return unlockCostId;
	}

	@Override
	public boolean assemble() {
		return true;
	}
	
	@Override
	public boolean checkValid() {
		return true;
	}
	
}
