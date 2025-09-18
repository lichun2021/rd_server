package com.hawk.activity.type.impl.planetexploration.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRand;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

@HawkConfigManager.XmlResource(file = "activity/planet_exploration/planet_exploration_reward.xml")
public class PlanetExploreRewardCfg extends HawkConfigBase {
	
	private final int id;
	
	private final String reward;
	
	private final int weight;
	
	private static List<String> rewardList = new ArrayList<String>();
	private static List<Integer> weights = new ArrayList<Integer>();
	
	public PlanetExploreRewardCfg(){
		this.id = 0;
		this.reward = "";
		this.weight = 0;
	}
	
	public boolean assemble() {
		rewardList.add(reward);
		weights.add(weight);
		return super.assemble();
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(reward);
		if (!valid) {
			throw new InvalidParameterException(String.format("PlanetExploreRewardCfg reward error, id: %s , reward: %s", id, reward));
		}
		return super.checkValid();
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
	
	/**
	 * 随机奖励
	 * @param times
	 * @return
	 */
	public static List<RewardItem.Builder> rewardRandom(int times) {
		List<RewardItem.Builder> list = new ArrayList<>();
		while (times > 0) {
			times--;
			String rewardItem = HawkRand.randomWeightObject(rewardList, weights);
			list.add(RewardHelper.toRewardItem(rewardItem));
		}
		return list;
	}
	
}
