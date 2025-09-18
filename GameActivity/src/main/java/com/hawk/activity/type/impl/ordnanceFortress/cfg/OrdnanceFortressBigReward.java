package com.hawk.activity.type.impl.ordnanceFortress.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 时空好礼时空之门直购礼包道具
 * 
 * @author che
 *
 */
@HawkConfigManager.XmlResource(file = "activity/ordnance_fortress/ordnance_fortress_big_reward.xml")
public class OrdnanceFortressBigReward extends HawkConfigBase {
	// 奖励ID
	@Id
	private final int id;
	
	private final int pool;
	
	private final String reward;
	
	private final int maxLimit;
	
	
	

	public OrdnanceFortressBigReward() {
		id = 0;
		pool = 0;
		reward = "";
		maxLimit= 100;
	}
	
	@Override
	protected boolean assemble() {
		return true;
	}

	public int getId() {
		return id;
	}

	public int getPool() {
		return pool;
	}

	public String getReward() {
		return reward;
	}

	public int getMaxLimit() {
		return maxLimit;
	}
	
	

	

	

}
