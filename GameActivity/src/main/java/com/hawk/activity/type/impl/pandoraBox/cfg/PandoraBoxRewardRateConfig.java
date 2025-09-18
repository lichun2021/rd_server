package com.hawk.activity.type.impl.pandoraBox.cfg;

import java.security.InvalidParameterException;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager.XmlResource;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;
@XmlResource(file="activity/pandora_box/pandora_box_activity.xml")
public class PandoraBoxRewardRateConfig extends HawkConfigBase {
	@Id
	private final int id;
	
	private final int rate;
	
	private final String reward;
	private List<RewardItem.Builder> rewardList;
	public int getId() {
		return id;
	}

	public int getRate() {
		return rate;
	}

	public String getReward() {
		return reward;
	}
	
	public PandoraBoxRewardRateConfig() {
		this.id = 0;
		this.rate = 0;
		this.reward = "";
	}
	
	@Override
	protected boolean assemble() {
		rewardList = RewardHelper.toRewardItemImmutableList(reward);
		
		return true;
	}

	@Override
	protected boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(getReward());
		if(!valid){
			throw new InvalidParameterException(String.format("reward error, id: %d, Class name: %s ", getId(), getClass().getName())); 
		}
		if(rate < 0){
			throw new InvalidParameterException(String.format("rate error, rate: %d, Class name: %s ", getRate(), getClass().getName()));
		}
		return super.checkValid();
	}

	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}
}
