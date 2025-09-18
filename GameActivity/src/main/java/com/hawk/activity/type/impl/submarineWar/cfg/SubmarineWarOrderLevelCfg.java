package com.hawk.activity.type.impl.submarineWar.cfg;

import java.security.InvalidParameterException;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

/**
 *  中部养成计划
 * 
 * @author che
 *
 */
@HawkConfigManager.XmlResource(file = "activity/submarine_war/submarine_war_pass_level.xml")
public class SubmarineWarOrderLevelCfg extends HawkConfigBase {
	// 奖励ID
	@Id
	private final int level;
	private final int levelUpExp;
	private final String normalReward;
	private final String advReward;
	
	
	public SubmarineWarOrderLevelCfg() {
		level = 0;
		levelUpExp = 0;
		normalReward = "";
		advReward = "";
	}
	
	@Override
	protected boolean assemble() {
		return true;
	}

	public int getLevel() {
		return level;
	}
	
	
	public int getLevelUpExp() {
		return levelUpExp;
	}
	
	
	public List<RewardItem.Builder> getNormalRewardList() {
		return RewardHelper.toRewardItemImmutableList(this.normalReward);
	}
	
	public List<RewardItem.Builder> getAdvRewardList() {
		return RewardHelper.toRewardItemImmutableList(this.advReward);
	}

	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(this.normalReward);
		if (!valid) {
			throw new InvalidParameterException(String.format("SubmarineWarOrderLevelCfg reward error, id: %s , reward: %s", level, normalReward));
		}
		valid = ConfigChecker.getDefaultChecker().checkAwardsValid(this.advReward);
		if (!valid) {
			throw new InvalidParameterException(String.format("SubmarineWarOrderLevelCfg reward error, id: %s , reward: %s", level, advReward));
		}
		return super.checkValid();
	}
	
	
	
	

}
