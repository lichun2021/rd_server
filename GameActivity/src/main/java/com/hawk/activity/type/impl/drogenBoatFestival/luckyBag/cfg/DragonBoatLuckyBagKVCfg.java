package com.hawk.activity.type.impl.drogenBoatFestival.luckyBag.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 端午-联盟庆典配置
 * 
 * @author che
 *
 */
@HawkConfigManager.KVResource(file = "activity/dw_gold/dw_gold_cfg.xml")
public class DragonBoatLuckyBagKVCfg extends HawkConfigBase {

	//服务器开服延时开启活动时间
	private final int serverDelay;
	
	private final String cost;
	
	private final String gain;
	
	private final int buyLimit;
	
	public DragonBoatLuckyBagKVCfg() {
		serverDelay = 0;
		cost = "";
		gain = "";
		buyLimit = 0;
		
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public String getCost() {
		return cost;
	}

	public int getBuyLimit() {
		return buyLimit;
	}

	public String getGain() {
		return gain;
	}

	
	

	
	
	
	
	
	
}
