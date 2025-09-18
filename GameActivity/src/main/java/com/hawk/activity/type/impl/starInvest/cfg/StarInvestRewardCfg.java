package com.hawk.activity.type.impl.starInvest.cfg;

import java.security.InvalidParameterException;
import java.util.List;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;
																	
@HawkConfigManager.XmlResource(file = "activity/star_invest/star_invest_reward.xml")
public class StarInvestRewardCfg extends HawkConfigBase {
	@Id
	private final int id;
	//累计几天
	private final int day;
	//等级
	private final int pool;
	//奖励100
	private final String first;
	//奖励300
	private final String second;
	//奖励500
	private final String third;

	//奖励
	private List<RewardItem.Builder> rewardFirst;
	private List<RewardItem.Builder> rewardSecond;
	private List<RewardItem.Builder> rewardThird;
	
	public StarInvestRewardCfg(){
		id = 0;
		day = 0;
		pool = 0;
		first = "";
		second = "";
		third = "";
		
	}

	@Override
	protected boolean assemble() {
		rewardFirst = RewardHelper.toRewardItemImmutableList(first);
		rewardSecond = RewardHelper.toRewardItemImmutableList(second);
		rewardThird = RewardHelper.toRewardItemImmutableList(third);
		return super.assemble();
	}



	public int getId() {
		return id;
	}

	public List<RewardItem.Builder> getRewardFirst() {
		return rewardFirst;
	}

	public void setRewardFirst(List<RewardItem.Builder> rewardFirst) {
		this.rewardFirst = rewardFirst;
	}

	public List<RewardItem.Builder> getRewardSecond() {
		return rewardSecond;
	}

	public void setRewardSecond(List<RewardItem.Builder> rewardSecond) {
		this.rewardSecond = rewardSecond;
	}

	public List<RewardItem.Builder> getRewardThird() {
		return rewardThird;
	}

	public void setRewardThird(List<RewardItem.Builder> rewardThird) {
		this.rewardThird = rewardThird;
	}

	public int getDay() {
		return day;
	}

	public int getPool() {
		return pool;
	}

	public String getFirst() {
		return first;
	}

	public String getSecond() {
		return second;
	}

	public String getThird() {
		return third;
	}

	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(this.first);
		if (!valid) {
			throw new InvalidParameterException(String.format("StarInvestRewardCfg reward error, id: %s , reward: %s", id, first));
		}
		valid = ConfigChecker.getDefaultChecker().checkAwardsValid(this.second);
		if (!valid) {
			throw new InvalidParameterException(String.format("StarInvestRewardCfg reward error, id: %s , reward: %s", id, second));
		}
		valid = ConfigChecker.getDefaultChecker().checkAwardsValid(this.third);
		if (!valid) {
			throw new InvalidParameterException(String.format("StarInvestRewardCfg reward error, id: %s , reward: %s", id, third));
		}
		return super.checkValid();
	}
	
}
