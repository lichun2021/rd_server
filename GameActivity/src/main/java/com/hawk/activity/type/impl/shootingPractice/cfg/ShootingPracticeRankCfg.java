package com.hawk.activity.type.impl.shootingPractice.cfg;

import java.security.InvalidParameterException;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

@HawkConfigManager.XmlResource(file = "activity/shooting_practice/shooting_practice_rank_reward.xml")
public class ShootingPracticeRankCfg extends HawkConfigBase {
	
	@Id
	private final int id;
	
	private final int rankUpper;
	
	private final int rankLower;
	
	private final String reward;
	
	public ShootingPracticeRankCfg(){
		this.id = 0;
		this.rankUpper = 0;
		this.rankLower = 0;
		this.reward = "";
	}
	
	@Override
	protected boolean assemble() {
		return true;
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(reward);
		if (!valid) {
			throw new InvalidParameterException(String.format("ShootingPracticeRankCfg reward error, id: %s , reward: %s", id, reward));
		}
		return super.checkValid();
	}

	public int getId() {
		return id;
	}

	public int getRankHigh() {
		return rankUpper;
	}

	public int getRankLow() {
		return rankLower;
	}


	public List<RewardItem.Builder> getRewardList() {
		return RewardHelper.toRewardItemImmutableList(reward);
	}
}
