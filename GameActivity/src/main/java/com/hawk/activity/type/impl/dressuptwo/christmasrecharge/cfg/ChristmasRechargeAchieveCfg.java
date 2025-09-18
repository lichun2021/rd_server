package com.hawk.activity.type.impl.dressuptwo.christmasrecharge.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;

import java.util.List;


/**
 * 圣诞节系列活动 累积充值
 * @author hf
 *
 */
@HawkConfigManager.XmlResource(file = "activity/christmas_recharge/christmas_recharge_achieve.xml")
public class ChristmasRechargeAchieveCfg extends AchieveConfig {
	/** 成就id*/
	@Id
	private final int achieveId;
	/** 条件类型*/
	private final int conditionType;
	/** 条件值*/
	private final String conditionValue;
	/** 奖励列表*/
	private final String rewards;
	/** 随机金条*/
	private final String randDiamond;
	
	private AchieveType achieveType;
	private List<RewardItem.Builder> rewardList;
	private List<Integer> conditionValueList;

	public ChristmasRechargeAchieveCfg() {
		achieveId = 0;
		conditionType = 0;
		conditionValue = "";
		rewards = "";
		randDiamond = "";
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
	
	public int randDiamod() {
		if (HawkOSOperator.isEmptyString(randDiamond)) {
			return 0;
		}
		String[] split = randDiamond.split("_");
		int min = Integer.parseInt(split[0]);
		int max = Integer.parseInt(split[1]);
		return HawkRand.randInt(min, max);
	}
}

