package com.hawk.activity.type.impl.prestressingloss.cfg;

import java.util.List;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.serialize.string.SerializeHelper;


@HawkConfigManager.XmlResource(file = "activity/loss/loss_activity_achieve.xml")
public class PrestressingLossAchieveCfg extends AchieveConfig {
	/** 成就id */
	@Id
	private final int achieveId;
	/** 条件类型 */
	private final int conditionType;
	/** 条件值 */
	private final String conditionValue;
	/** 奖励列表 */
	private final String rewards;
	
	private final int lossDay;
	private final String buildLevel;
	private final String vipLevel;

	private AchieveType achieveType;
	private List<RewardItem.Builder> rewardList;
	private List<Integer> conditionValueList;
	private int startCityLv, endCityLv, startVipLevel, endVipLevel;

	public PrestressingLossAchieveCfg() {
		achieveId = 0;
		conditionType = 0;
		conditionValue = "";
		rewards = "";
		lossDay = 0;
		buildLevel = "";
		vipLevel = "";
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
		
		if (!HawkOSOperator.isEmptyString(buildLevel)) {
			String[] buildLevelSeg = buildLevel.split("_");
			startCityLv = Integer.parseInt(buildLevelSeg[0]);
			endCityLv = Integer.parseInt(buildLevelSeg[1]);
		}
		
		if (!HawkOSOperator.isEmptyString(vipLevel)) {
			String[] vipLevelSeg = vipLevel.split("_");
			startVipLevel = Integer.parseInt(vipLevelSeg[0]);
			endVipLevel = Integer.parseInt(vipLevelSeg[1]);
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

	public int getStartCityLv() {
		return startCityLv;
	}

	public int getEndCityLv() {
		return endCityLv;
	}

	public int getStartVipLevel() {
		return startVipLevel;
	}

	public int getEndVipLevel() {
		return endVipLevel;
	}

	public int getLossDay() {
		return lossDay;
	}

}
