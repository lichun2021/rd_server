package com.hawk.activity.type.impl.seasonpuzzle.cfg;

import java.util.ArrayList;
import java.util.List;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.XmlResource(file = "activity/season_picture_puzzle/season_picture_puzzle_achieve.xml")
public class SeasonPuzzleAchieveCfg extends AchieveConfig {
	/** 成就id*/
	@Id
	private final int achieveId;
	/** 条件类型*/
	private final int conditionType;
	/** 条件值*/
	private final String conditionValue;
	/** 奖励列表*/
	private final String rewards;
	
	private final int periods;
	private final int rewardsOdds;
	
	private final int reset;
	
	/** 每日可以完成任务的次数  */
	private final int getTimesLimit;
	
	private AchieveType achieveType;
	private List<RewardItem.Builder> rewardList;
	private List<Integer> conditionValueList;

	private List<String> rewardStrList = new ArrayList<>();
	private List<Integer> rewardWeightList = new ArrayList<>();
	
	public SeasonPuzzleAchieveCfg() {
		achieveId = 0;
		conditionType = 0;
		conditionValue = "";
		rewards = "";
		periods = 0;
		rewardsOdds = 0;
		reset = 0;
		getTimesLimit = 1;
	}
	
	@Override
	protected boolean assemble() {
		try {
			achieveType = AchieveType.getType(conditionType);
			if (achieveType == null) {
				HawkLog.errPrintln("achieve type not found! type: {}", conditionType);
				return false;
			}
			if (achieveType == AchieveType.SEASON_PUZZLE_COMPLETE_VALUE) {
				rewardList = RewardHelper.toRewardItemImmutableList(rewards);
			} else {
				String[] arr = rewards.split(",");
				for (String item : arr) {
					String[] itemStr = item.split("_");
					rewardStrList.add(String.format("%s_%s_%s", itemStr[0], itemStr[1], itemStr[2]));
					rewardWeightList.add(Integer.parseInt(itemStr[3]));
				}
			}
			conditionValueList = SerializeHelper.stringToList(Integer.class, conditionValue, SerializeHelper.ATTRIBUTE_SPLIT);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}
	
	public List<String> getRewardStrList() {
		return rewardStrList;
	}
	
	public List<Integer> getRewardWeightList() {
		return rewardWeightList;
	}
	
	@Override
	public int getAchieveId() {
		return achieveId;
	}

	@Override
	public AchieveType getAchieveType() {
		return achieveType;
	}

	@Override
	public List<Integer> getConditionValues() {
		return conditionValueList;
	}

	@Override
	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}

	@Override
	public String getReward() {
		return rewards;
	}

	public int getPeriods() {
		return periods;
	}

	public int getRewardsOdds() {
		return rewardsOdds;
	}

	public int getReset() {
		return reset;
	}

	public int getGetTimesLimit() {
		return getTimesLimit;
	}
}
