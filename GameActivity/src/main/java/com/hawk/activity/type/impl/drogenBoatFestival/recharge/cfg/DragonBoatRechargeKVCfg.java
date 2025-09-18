package com.hawk.activity.type.impl.drogenBoatFestival.recharge.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 端午庆典
 * @author che
 *
 */
@HawkConfigManager.KVResource(file = "activity/dw_topup/dw_topup_cfg.xml")
public class DragonBoatRechargeKVCfg extends HawkConfigBase {
	
	/** 服务器开服延时开启活动时间*/
	private final int serverDelay;
	
	
	/** 需要完成的成就ID*/
	private final int finishId;
	
	public DragonBoatRechargeKVCfg() {
		serverDelay = 0;
		finishId = 0;
		
	}

	
	public long getServerDelay() {
		return serverDelay * 1000l;
	}


	
	public int getFinishId() {
		return finishId;
	}

	
}