package com.hawk.activity.type.impl.spaceguard.cfg;

import java.util.List;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.serialize.string.SerializeHelper;
																	
@HawkConfigManager.XmlResource(file = "activity/space_machine_guard/space_machine_guard_point_award.xml")
public class SpaceGuardAchieveCfg extends AchieveConfig {
	/** 成就id */
	@Id
	private final int achieveId;

	/** 条件类型 */
	private final int conditionType;

	/** 条件值 */
	private final String conditionValue;

	/** 奖励列表 */
	private final String reward;

	private AchieveType achieveType;

	private List<RewardItem.Builder> rewardList;

	private List<Integer> conditionValueList;

	public SpaceGuardAchieveCfg() {
		achieveId = 0;
		conditionType = 0;
		conditionValue = "";
		reward = "";
	}

	@Override
	protected boolean assemble() {
		try {
			achieveType = AchieveType.getType(conditionType);
			if (achieveType == null) {
				HawkLog.errPrintln("achieve type not found! type: {}", conditionType);
				return false;
			}
			rewardList = RewardHelper.toRewardItemImmutableList(reward);
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
		return reward;
	}
	
}
