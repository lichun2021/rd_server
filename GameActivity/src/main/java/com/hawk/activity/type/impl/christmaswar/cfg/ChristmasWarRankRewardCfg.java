package com.hawk.activity.type.impl.christmaswar.cfg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

@HawkConfigManager.XmlResource(file = "activity/christmas_war/christmas_war_rank_reward.xml")
public class ChristmasWarRankRewardCfg extends HawkConfigBase {
	@Id
	private final int id;
	/**
	 * 排行榜类型
	 */
	private final int rankType;
	/**
	 * 排名上限
	 */
	private final int rankUpper;
	/**
	 * 排名下限
	 */
	private final int rankLower;
	/**
	 * 奖励
	 */
	private final String rewards;
	/**
	 * {@link #rewards}
	 */
	private List<RewardItem.Builder> rewardList;
	
	public ChristmasWarRankRewardCfg() {
		this.id = 0;
		this.rankType = 0;
		this.rankUpper = 0;
		this.rankLower = 0;
		this.rewards = "";
	}
	
	public int getId() {
		return id;
	}
	public int getRankType() {
		return rankType;
	}
	public int getRankUpper() {
		return rankUpper;
	}
	public int getRankLower() {
		return rankLower;
	}
	public String getRewards() {
		return rewards;
	}
	
	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}
	
	@Override
	public boolean assemble() {
		if (!HawkOSOperator.isEmptyString(rewards)) {
			rewardList = Collections.synchronizedList(RewardHelper.toRewardItemImmutableList(rewards));
		} else {
			rewardList = Collections.synchronizedList(new ArrayList<>());
		}
		
		return true;
	}
	
	@Override
	public boolean checkValid() {		
		return ConfigChecker.getDefaultChecker().checkAwardsValid(rewards);				
	}
}
