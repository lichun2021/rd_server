package com.hawk.activity.type.impl.machineLab.cfg;

import java.security.InvalidParameterException;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

@HawkConfigManager.XmlResource(file = "activity/machine_lab/machine_lab_level.xml")
public class MachineLabLevel extends HawkConfigBase {
	/**
	 * 唯一ID
	 */
	@Id
	private final int level;
	
	private final int levelExp;
	
	private final int playerLevelExp;
	
	private final String reward;
	
	private final String playerReward;
	
	private final String playerAdvReward;


	public MachineLabLevel() {
		level = 0;
		levelExp = 0;
		playerLevelExp = 0;
		reward = "";
		playerReward = "";
		playerAdvReward = "";
	}

	public int getlevel() {
		return level;
	}
	
	public int getLevelExp() {
		return levelExp;
	}
	
	public int getPlayerLevelExp() {
		return playerLevelExp;
	}

	
	public List<RewardItem.Builder> getRewardItemList() {
		return RewardHelper.toRewardItemImmutableList(this.reward);
	}
	
	public List<RewardItem.Builder> getPlayerRewardItemList() {
		return RewardHelper.toRewardItemImmutableList(this.playerReward);
	}
	
	public List<RewardItem.Builder> getPlayerAdvRewardItemList() {
		return RewardHelper.toRewardItemImmutableList(this.playerAdvReward);
	}
	
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(reward);
		if (!valid) {
			throw new InvalidParameterException(String.format("MachineLabLevel needItem error, id: %s , needItem: %s", level, reward));
		}
		valid = ConfigChecker.getDefaultChecker().checkAwardsValid(playerReward);
		if (!valid) {
			throw new InvalidParameterException(String.format("MachineLabLevel gainItem error, id: %s , gainItem: %s", level, playerReward));
		}
		valid = ConfigChecker.getDefaultChecker().checkAwardsValid(playerAdvReward);
		if (!valid) {
			throw new InvalidParameterException(String.format("MachineLabLevel gainItem error, id: %s , gainItem: %s", level, playerAdvReward));
		}
		return super.checkValid();
	}

}
