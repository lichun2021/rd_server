package com.hawk.activity.type.impl.battlefield.cfg;

import java.util.HashMap;
import java.util.Map;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 战地寻宝活动配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "activity/battlefield_treasure/battlefield_passport_award.xml")
public class BattleFieldDailyLoginAwardCfg extends HawkConfigBase {
	@Id
	private final int day;
	/** 奖励*/
	private final String rewards;
	
	private static Map<Integer, String> dailyAwards = new HashMap<Integer, String>();
	
	public BattleFieldDailyLoginAwardCfg() {
		day = 0;
		rewards = "";
	}
	
	public String getRewards() {
		return rewards;
	}
	
	public boolean assemble() {
		dailyAwards.put(day, rewards);
		return true;
	}
	
	public static String getAward(int loginDays) {
		return dailyAwards.getOrDefault(loginDays, "");
	}

}
