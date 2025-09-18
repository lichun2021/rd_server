package com.hawk.activity.type.impl.energyInvest.cfg;

import java.util.List;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
																	
@HawkConfigManager.XmlResource(file = "activity/power_invest/power_invest_reward.xml")
public class EnergyInvestRewardCfg extends HawkConfigBase {
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
	
	public EnergyInvestRewardCfg(){
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

	
}
