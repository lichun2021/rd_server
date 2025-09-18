package com.hawk.activity.type.impl.bannerkill.cfg;

import java.security.InvalidParameterException;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

@HawkConfigManager.XmlResource(file = "activity/flag_god/activity_flaggod_rank.xml")
public class ActivityBannerKillRankCfg extends HawkConfigBase {
	@Id
	private final int id;
	/**
	 * 大本等级下限
	 */
	private final int lvMin;
	/**
	 * 打本等级上限
	 */
	private final int lvMax;
	/**
	 * 排名上限包含该等级
	 */
	private final int rankUpper;
	/**
	 * 排名下限
	 */
	private final int rankLower;
	/**
	 * 奖励
	 */
	private final String reward;
	/**
	 * 奖励列表
	 */
	private List<RewardItem.Builder> rewardList;
	
	public ActivityBannerKillRankCfg() {
		id = 0;
		lvMin = 0;
		lvMax = 0;
		rankUpper = 0;
		rankLower = 0;
		reward = "";
	}
	
	public int getId() {
		return id;
	}
	
	public int getLvMin() {
		return lvMin;
	}
	public int getLvMax() {
		return lvMax;
	}
	public int getRankUpper() {
		return rankUpper;
	}
	public int getRankLower() {
		return rankLower;
	}
	public String getReward() {
		return reward;
	}
	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}
	public void setRewardList(List<RewardItem.Builder> rewardList) {
		this.rewardList = rewardList;
	}
	
	public boolean assemble() {
		if (HawkOSOperator.isEmptyString(reward)) {
			return false;
		}
		
		this.rewardList = RewardHelper.toRewardItemImmutableList(reward);
		
		return true;
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(reward);
		if (!valid) {
			throw new InvalidParameterException(String.format("ActivityBannerKillRankCfg reward error, id: %s , item: %s", id, reward));
		}
		return super.checkValid();
	}
}
