package com.hawk.activity.type.impl.drogenBoatFestival.guildCelebration.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 端午-联盟庆典配置
 * 
 * @author che
 *
 */
@HawkConfigManager.KVResource(file = "activity/dw_gift/dw_gift_cfg.xml")
public class DragonBoatCelebrationKVCfg extends HawkConfigBase {

	//服务器开服延时开启活动时间
	private final int serverDelay;

	//制作礼物消耗
	private final String makeGiftCost;
	
	private final String makeGiftAchieve;
	
	private final int makeGiftExp;
	
	//装扮礼物树消耗
	private final String dreeTreeCost;
	
	private final String dreeTreeAchieve;
	
	private final int dreeTreeExp;
	

	public DragonBoatCelebrationKVCfg() {
		serverDelay = 0;
		makeGiftCost = "";
		makeGiftAchieve = "";
		makeGiftExp = 0;
		
		dreeTreeCost = "";
		dreeTreeAchieve = "";
		dreeTreeExp = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public String getMakeGiftCost() {
		return makeGiftCost;
	}

	public String getMakeGiftAchieve() {
		return makeGiftAchieve;
	}

	public String getDreeTreeCost() {
		return dreeTreeCost;
	}

	public String getDreeTreeAchieve() {
		return dreeTreeAchieve;
	}

	public int getMakeGiftExp() {
		return makeGiftExp;
	}

	public int getDreeTreeExp() {
		return dreeTreeExp;
	}

	

	
	
	
	
	
}
