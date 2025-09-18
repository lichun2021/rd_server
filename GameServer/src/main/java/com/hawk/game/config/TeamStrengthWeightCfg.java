package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 赛博之战出战奖励配置
 * 
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/team_strength_weight.xml")
public class TeamStrengthWeightCfg extends HawkConfigBase {
	@Id
	private final int id;
	
	private final int type;
	
	private final int rankUpper;
	
	private final int rankLower;
	
	private final double weightValue;
	
	
	public TeamStrengthWeightCfg() {
		id = 0;
		type = 0;
		rankUpper = 0;
		rankLower = 0;
		weightValue = 0;
		
	}

	public int getId() {
		return id;
	}

	public int getType() {
		return type;
	}
	
	
	public int getRankLower() {
		return rankLower;
	}

	
	public int getRankUpper() {
		return rankUpper;
	}
	
	
	public double getWeightValue() {
		return weightValue / 10000;
	}
	

	@Override
	protected boolean checkValid() {
		if (this.rankUpper > this.rankLower) {
			return false;
		}
		return true;
	}
}
