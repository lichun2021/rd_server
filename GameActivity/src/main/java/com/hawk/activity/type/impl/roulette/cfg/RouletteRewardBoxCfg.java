package com.hawk.activity.type.impl.roulette.cfg;

import java.security.InvalidParameterException;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager.XmlResource;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;
@XmlResource(file="activity/roulette/roulette_reward_box.xml")
public class RouletteRewardBoxCfg extends HawkConfigBase {
	@Id
	private final int id;
	
	private final int score;
	
	private final int times;
	private final String reward;
	private List<RewardItem.Builder> rewardList;

	public int getId() {
		return id;
	}

	public int getScore() {
		return score;
	}

	public String getReward() {
		return reward;
	}
	
	public RouletteRewardBoxCfg() {
		this.id = 0;
		this.score = 0;
		this.reward = "";
		this.times = 1;
	}
	
	@Override
	protected boolean assemble() {
		try{
			rewardList = RewardHelper.toRewardItemImmutableList(reward);
		}catch(Exception e){
			HawkException.catchException(e);
			return false;
		}
		return true;
	}

	@Override
	protected boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(getReward());
		if(!valid){
			throw new InvalidParameterException(String.format("reward error, id: %d, Class name: %s ", getId(), getClass().getName())); 
		}
		return super.checkValid();
	}

	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}

	public int getTimes() {
		return this.times;
	}
}
