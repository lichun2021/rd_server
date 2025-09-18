package com.hawk.activity.type.impl.armiesMass.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.protocol.Activity.PBSculptureQuality;

/**
 * 时空好礼时空之门直购礼包道具
 * 
 * @author che
 *
 */
@HawkConfigManager.XmlResource(file = "activity/armies_mass/armies_mass_reward.xml")
public class ArmiesMassSculptureAwardCfg extends HawkConfigBase {
	// 奖励ID
	@Id
	private final int id;
	//品质
	private final int pool;
	//阶段
	private final String reward;

	private final int weight;

	public ArmiesMassSculptureAwardCfg() {
		id = 0;
		pool = 0;
		reward = "";
		weight = 0;
	}
	
	@Override
	protected boolean assemble() {
		if(PBSculptureQuality.valueOf(this.pool) == null){
			return false;
		}
		return true;
	}

	public int getPool() {
		return pool;
	}


	public int getId() {
		return id;
	}

	public String getReward() {
		return reward;
	}

	public int getWeight() {
		return weight;
	}

	
	
	

	


}
