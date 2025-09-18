package com.hawk.activity.type.impl.logingift.cfg;

import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "activity/login_gifts/%s/login_gifts_sign.xml", autoLoad=false, loadParams="313")
public class LoginGiftRewardCfg extends HawkConfigBase {
	@Id
	private final int id;
	
	/** 天数 */
	private final int day;

	/** 普通奖励 */
	private final String commonRewards;

	/** 进阶奖励 */
	private final String advancedReward;
	
	private static Map<Integer, LoginGiftRewardCfg> dayMap = new HashMap<Integer, LoginGiftRewardCfg>();

	public LoginGiftRewardCfg() {
		id = 0;
		day = 0;
		commonRewards = "";
		advancedReward = "";
	}
	
	public int getId() {
		return id;
	}

	public int getDay() {
		return day;
	}

	public String getCommonRewards() {
		return commonRewards;
	}

	public String getAdvancedReward() {
		return advancedReward;
	}
	
	public boolean assemble() {
		dayMap.put(day, this);
		return true;
	}
	
	public static LoginGiftRewardCfg getConfig(int day) {
		return dayMap.get(day);
	}

}
