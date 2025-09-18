package com.hawk.activity.type.impl.drogenBoatFestival.guildCelebration.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 端午-联盟庆典 等级配置
 * 
 * @author che
 *
 */
@HawkConfigManager.XmlResource(file = "activity/dw_gift/dw_gift_exchange.xml")
public class DragonBoatCelebrationExchangeCfg extends HawkConfigBase {
	// 奖励ID
	@Id
	private final int id;
	// 对应礼包ID
	private final String pay;
	// 奖励内容
	private final String gain;
	
	private final int expGain;
	
	private final int exchangeLimit;

	public DragonBoatCelebrationExchangeCfg() {
		id = 0;
		pay = "";
		gain = "";
		expGain = 0;
		exchangeLimit = 0;
	}
	
	@Override
	protected boolean assemble() {
		return true;
	}

	public int getId() {
		return id;
	}

	

	public String getPay() {
		return pay;
	}

	public String getGain() {
		return gain;
	}

	public int getExpGain() {
		return expGain;
	}

	public int getExchangeLimit() {
		return exchangeLimit;
	}

	
	
	

	

}
