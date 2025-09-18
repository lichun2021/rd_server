package com.hawk.activity.type.impl.plantsecret.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 泰能机密
 * 
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "activity/taineng_secret/taineng_secret_daily_key.xml")
public class PlantSecretDailyReward extends HawkConfigBase {
	// 奖励ID
	@Id
	private final int id;
	
	private final int daily_reward;
	
	private final String key;
	
	public PlantSecretDailyReward() {
		id = 0;
		daily_reward = 0;
		key = "";
	}
	
	@Override
	protected boolean assemble() {
		return true;
	}

	public int getId() {
		return id;
	}

	public int getDaily_reward() {
		return daily_reward;
	}

	public String getKey() {
		return key;
	}

}
