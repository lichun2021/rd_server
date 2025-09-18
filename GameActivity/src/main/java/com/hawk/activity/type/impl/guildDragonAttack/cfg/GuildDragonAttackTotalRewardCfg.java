package com.hawk.activity.type.impl.guildDragonAttack.cfg;

import java.security.InvalidParameterException;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.gamelib.activity.ConfigChecker;

@HawkConfigManager.XmlResource(file = "activity/alliance_boss/alliance_boss_allince_reward.xml")
public class GuildDragonAttackTotalRewardCfg extends HawkConfigBase {
	@Id
	private final int id;
	/**
	 * 排名上限包含该等级
	 */
	private final int allianceDamagePoint;
	/**
	 * 奖励
	 */
	private final String reward;
	
	public GuildDragonAttackTotalRewardCfg() {
		id = 0;
		allianceDamagePoint = 0;
		reward = "";
	}
	
	public int getId() {
		return id;
	}
	
	
	public int getAllianceDamagePoint() {
		return allianceDamagePoint;
	}
	
	public String getReward() {
		return reward;
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(reward);
		if (!valid) {
			throw new InvalidParameterException(String.format("AllianceBossReward reward error, id: %s , item: %s", id, reward));
		}
		return super.checkValid();
	}
}
