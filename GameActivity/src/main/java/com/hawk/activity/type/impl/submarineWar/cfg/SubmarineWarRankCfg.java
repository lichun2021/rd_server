package com.hawk.activity.type.impl.submarineWar.cfg;

import java.security.InvalidParameterException;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.gamelib.activity.ConfigChecker;

/**
 *  中部养成计划
 * 
 * @author che
 *
 */
@HawkConfigManager.XmlResource(file = "activity/submarine_war/submarine_war_rank.xml")
public class SubmarineWarRankCfg extends HawkConfigBase {
	// 奖励ID
	@Id
	private final int id;
	private final int rankUpper;
	private final int rankLower;
	private final String reward;
	private final String finalReward;
	
	private static int maxRank;
	
	
	public SubmarineWarRankCfg() {
		id = 0;
		rankUpper = 0;
		rankLower = 0;
		reward = "";
		finalReward= "";
	}
	
	protected boolean assemble() {
		if(this.rankLower > maxRank){
			maxRank = this.rankLower;
		}
		return true;
	}
	

	
	public int getRankUpper() {
		return rankUpper;
	}
	
	public int getRankLower() {
		return rankLower;
	}
	
	public String getReward() {
		return reward;
	}
	
	public String getFinalReward() {
		return finalReward;
	}
	

	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(reward);
		if (!valid) {
			throw new InvalidParameterException(String.format("SubmarineWarRankCfg reward error, id: %s , reward: %s", id, reward));
		}
		valid = ConfigChecker.getDefaultChecker().checkAwardsValid(finalReward);
		if (!valid) {
			throw new InvalidParameterException(String.format("SubmarineWarRankCfg finalReward error, id: %s , finalReward: %s", id, finalReward));
		}
		return super.checkValid();
	}
	
	
	public static int getMaxRank() {
		return maxRank;
	}

}
