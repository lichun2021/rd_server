package com.hawk.activity.type.impl.drogenBoatFestival.guildCelebration.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 端午-联盟庆典 等级配置
 * 
 * @author che
 *
 */
@HawkConfigManager.XmlResource(file = "activity/dw_gift/dw_gift_lv.xml")
public class DragonBoatCelebrationLevelCfg extends HawkConfigBase {
	// 奖励ID
	@Id
	private final int lv;
	//经验值
	private final int exp;
	// 奖励内容
	private final String reward;
	
	
	public DragonBoatCelebrationLevelCfg() {
		lv = 0;
		exp = 0;
		reward = "";
	}
	
	@Override
	protected boolean assemble() {
		return true;
	}

	public int getLv() {
		return lv;
	}

	public int getExp() {
		return exp;
	}

	public String getReward() {
		return reward;
	}


	

	
	

	

}
