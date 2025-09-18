
package com.hawk.activity.type.impl.appointget.cfg;

import java.util.List;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 推广员系统成就配置表 
 * @author RickMei 
 *
 */
@HawkConfigManager.XmlResource(file = "activity/appoint_get/appoint_get_achieve.xml")
public class AppointGetAchieveCfg extends AchieveConfig {
	// <data achieveId="331001" conditionType="140900" conditionValue="1" rewards="30000_1400009_1" isReset="1" />
	/** 成就id*/
	@Id
	private final int achieveId;
	/** 条件类型*/
	private final int conditionType;
	/** 条件值*/
	private final String conditionValue;
	/** 奖励列表*/
	private final String rewards;

	private final int isReset;

	private AchieveType achieveType;
	private List<RewardItem.Builder> rewardList;
	private List<Integer> conditionValueList;

	public AppointGetAchieveCfg() {
		achieveId = 0;
		conditionType = 0;
		conditionValue = "";
		rewards = "";
		isReset = 0;
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

	public String getRewards() {
		return rewards;
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

	public List<Integer> getConditionValueList() {
		return conditionValueList;
	}

	public void setConditionValueList(List<Integer> conditionValueList) {
		this.conditionValueList = conditionValueList;
	}

	public int getConditionType() {
		return conditionType;
	}

	public String getConditionValue() {
		return conditionValue;
	}

	public int getIsReset() {
		return isReset;
	}

	public void setAchieveType(AchieveType achieveType) {
		this.achieveType = achieveType;
	}

	public void setRewardList(List<RewardItem.Builder> rewardList) {
		this.rewardList = rewardList;
	}

}
