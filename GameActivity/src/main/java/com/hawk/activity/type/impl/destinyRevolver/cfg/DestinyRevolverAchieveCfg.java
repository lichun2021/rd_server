package com.hawk.activity.type.impl.destinyRevolver.cfg;

import java.util.List;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 命运左轮成就配置
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "activity/destiny_revolver/destiny_revolver_achieve.xml")
public class DestinyRevolverAchieveCfg extends AchieveConfig {

	/**
	 * 	成就id
	 */
	@Id
	private final int achieveId;
	
	/**
	 * 条件类型
	 */
	private final int conditionType;
	
	/**
	 * 条件值
	 */
	private final String conditionValue;
	
	/**
	 * 奖励列表
	 */
	private final String rewards;
	
	/**
	 * 成就类型
	 */
	private AchieveType achieveType;
	
	/**
	 * 奖励列表
	 */
	private List<RewardItem.Builder> rewardList;
	
	/**
	 * 条件值
	 */
	private List<Integer> conditionValueList;

	public DestinyRevolverAchieveCfg() {
		achieveId = 0;
		conditionType = 0;
		conditionValue = "";
		rewards = "";
	}

	public int getAchieveId() {
		return achieveId;
	}

	public int getConditionType() {
		return conditionType;
	}

	public String getConditionValue() {
		return conditionValue;
	}

	public String getReward() {
		return rewards;
	}

	public void setAchieveType(AchieveType achieveType) {
		this.achieveType = achieveType;
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
	protected boolean assemble() {
		achieveType = AchieveType.getType(conditionType);
		if (achieveType == null) {
			HawkLog.errPrintln("achieve type not found! type: {}", conditionType);
			return false;
		}
		rewardList = RewardHelper.toRewardItemImmutableList(rewards);
		conditionValueList = SerializeHelper.stringToList(Integer.class, conditionValue, SerializeHelper.ATTRIBUTE_SPLIT);
		return true;
	}
}
