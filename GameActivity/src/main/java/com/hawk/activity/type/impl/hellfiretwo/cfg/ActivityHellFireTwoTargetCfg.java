package com.hawk.activity.type.impl.hellfiretwo.cfg;

import java.security.InvalidParameterException;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

@HawkConfigManager.XmlResource(file = "activity/hellfire_two/activity_hellfire_two_target.xml")
public class ActivityHellFireTwoTargetCfg extends HawkConfigBase {
	/**
	 * 目标id
	 */
	@Id
	private final  int id;	
	/**
	 * 目标
	 */
	private final int targetId;
	/**
	 * 最小等级区间段
	 */
	private final int lvMin;
	/**
	 * 最大等级区间段
	 */
	private final int lvMax;
	/**
	 * 积分
	 */
	private final int score;
	/**
	 * 奖励
	 */
	private final String reward;
	/**
	 * 奖励
	 */
	private List<RewardItem.Builder> rewardList;
	/**
	 * 难度
	 */
	private final int difficultLevel;
	
	public ActivityHellFireTwoTargetCfg() {
		this.id = 0;
		this.targetId = 0;
		this.lvMax = 0;
		this.lvMin = 0;
		this.score = 0;
		this.reward ="";
		this.difficultLevel = 0;
	}
	
	public boolean assemble() {
		
		this.rewardList = RewardHelper.toRewardItemImmutableList(reward);
		return true;
	}
	
	@Override
	public boolean checkValid() {
		
		if(rewardList.isEmpty()) {
			throw new InvalidParameterException("ActivityHellFireTargetCfg reward is empty");
		}
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(reward);
		if (!valid) {
			throw new InvalidParameterException(String.format("ActivityHellFireTwoTargetCfg reward error, id: %s , item: %s", id, reward));
		}
		
		return true;		
	}

	public int getId() {
		return id;
	}

	public int getTargetId() {
		return targetId;
	}

	public int getLvMin() {
		return lvMin;
	}

	public int getLvMax() {
		return lvMax;
	}

	public int getScore() {
		return score;
	}

	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}

	public int getDifficultLevel() {
		return difficultLevel;
	}
}
