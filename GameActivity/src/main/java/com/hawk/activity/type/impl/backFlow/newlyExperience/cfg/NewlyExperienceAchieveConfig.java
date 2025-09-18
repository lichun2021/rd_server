package com.hawk.activity.type.impl.backFlow.newlyExperience.cfg;

import java.util.List;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigManager;
/***
 * 版本尝鲜任务
 * @author yang.rao
 *
 */

@HawkConfigManager.XmlResource(file = "activity/versionOftaste/versionOftaste_activity.xml")
public class NewlyExperienceAchieveConfig extends AchieveConfig {
	
	@Id
	private final int achieveId;
	
	private final int playerType;
	
	private final int conditionType;
	
	private final String conditionValue;
	
	private final String rewards;
	
	private AchieveType achieveType;
	private List<RewardItem.Builder> rewardList;
	private List<Integer> conditionValueList;
	
	public NewlyExperienceAchieveConfig(){
		achieveId = 0;
		playerType = 0;
		conditionType = 0;
		conditionValue = "";
		rewards = "";
	}
	
	@Override
	protected boolean assemble() {
		try {
			achieveType = AchieveType.getType(conditionType);
			if (achieveType == null) {
				HawkLog.errPrintln("achieve type not found! type: {}", conditionType);
				return false;
			}
			rewardList = RewardHelper.toRewardItemImmutableList(rewards);
			conditionValueList = SerializeHelper.stringToList(Integer.class, conditionValue, SerializeHelper.ATTRIBUTE_SPLIT);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}

	@Override
	public int getAchieveId() {
		return achieveId;
	}

	@Override
	public String getReward() {
		return rewards;
	}

	public int getConditionType() {
		return conditionType;
	}

	public String getConditionValue() {
		return conditionValue;
	}

	public String getRewards() {
		return rewards;
	}

	public AchieveType getAchieveType() {
		return achieveType;
	}

	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}

	public List<Integer> getConditionValueList() {
		return conditionValueList;
	}

	@Override
	public List<Integer> getConditionValues() {
		return conditionValueList;
	}

	public int getPlayerType() {
		return playerType;
	}

	
	
	
}
