package com.hawk.activity.type.impl.planetexploration.cfg;

import java.security.InvalidParameterException;
import java.util.List;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

@HawkConfigManager.XmlResource(file = "activity/planet_exploration/planet_exploration_rank_reward.xml")
public class PlanetExploreRankCfg extends HawkConfigBase {
	
	private final int id;
	
	private final int rankUpper;
	
	private final int rankLower;
	
	private final String rewards;
	
	private List<RewardItem.Builder> rewardList;
	
	public PlanetExploreRankCfg(){
		this.id = 0;
		this.rankUpper = 0;
		this.rankLower = 0;
		this.rewards = "";
	}
	
	@Override
	protected boolean assemble() {
		try {
			rewardList = RewardHelper.toRewardItemImmutableList(rewards);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(rewards);
		if (!valid) {
			throw new InvalidParameterException(String.format("PlanetExploreRankCfg reward error, id: %s , reward: %s", id, rewards));
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

	public String getGainItem() {
		return rewards;
	}

	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}
}
