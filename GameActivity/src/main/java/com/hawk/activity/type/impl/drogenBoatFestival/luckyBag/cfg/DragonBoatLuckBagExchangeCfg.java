package com.hawk.activity.type.impl.drogenBoatFestival.luckyBag.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 端午-联盟庆典 等级配置
 * 
 * @author che
 *
 */
@HawkConfigManager.XmlResource(file = "activity/dw_gold/dw_gold.xml")
public class DragonBoatLuckBagExchangeCfg extends HawkConfigBase {
	// 奖励ID
	@Id
	private final int id;
	// 对应礼包ID
	private final int lowLimit;
	// 奖励内容
	private final int highLimit;
	
	private final int rate;
	
	private final int dailyLimit;

	public DragonBoatLuckBagExchangeCfg() {
		id = 0;
		lowLimit = 0;
		highLimit = 0;
		rate = 0;
		dailyLimit = 0;
	}
	
	@Override
	protected boolean assemble() {
		return true;
	}

	public int getId() {
		return id;
	}

	public int getLowLimit() {
		return lowLimit;
	}

	public int getHighLimit() {
		return highLimit;
	}

	public int getRate() {
		return rate;
	}

	public int getDailyLimit() {
		return dailyLimit;
	}

	
	

	

	

}
