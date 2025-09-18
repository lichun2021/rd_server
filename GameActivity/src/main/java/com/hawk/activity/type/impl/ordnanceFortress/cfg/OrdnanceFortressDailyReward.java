package com.hawk.activity.type.impl.ordnanceFortress.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 时空好礼时空之门直购礼包道具
 * 
 * @author che
 *
 */
@HawkConfigManager.XmlResource(file = "activity/ordnance_fortress/ordnance_fortress_daily_key.xml")
public class OrdnanceFortressDailyReward extends HawkConfigBase {
	// 奖励ID
	@Id
	private final int id;
	
	private final int daily_reward;
	
	private final String key;
	
	

	public OrdnanceFortressDailyReward() {
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
