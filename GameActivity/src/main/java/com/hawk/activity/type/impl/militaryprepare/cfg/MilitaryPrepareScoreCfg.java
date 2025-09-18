package com.hawk.activity.type.impl.militaryprepare.cfg;

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
 * 军事备战活动积分奖励配置
 * @author Winder
 *
 */
@HawkConfigManager.XmlResource(file = "activity/military_prepare/military_prepare_score.xml")
public class MilitaryPrepareScoreCfg extends AchieveConfig {
	/** */
	@Id
	private final int achieveId;
	
	/** 条件类型*/
	private final int conditionType;
	/** 条件值*/
	private final String conditionValue;
	
	/** 奖励列表*/
	private final String rewards;
	
	/** 奖励列表*/
	private final String advancedRewards;
	
	private AchieveType achieveType;
	private List<RewardItem.Builder> rewardList;
	private List<RewardItem.Builder> advancedRewardList;
	private List<Integer> conditionValueList;
	
	public MilitaryPrepareScoreCfg() {
		achieveId = 0;
		rewards = "";
		conditionType = 0;
		conditionValue = "";
		advancedRewards = "";
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
			advancedRewardList = RewardHelper.toRewardItemImmutableList(advancedRewards);
			conditionValueList = SerializeHelper.stringToList(Integer.class, conditionValue, SerializeHelper.ATTRIBUTE_SPLIT);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}
	
	public int getAchieveId() {
		return achieveId;
	}

	public String getRewards() {
		return rewards;
	}

	public List<RewardItem.Builder> getRewardList() {
		return RewardHelper.toRewardItemList(rewards);
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
	public String getReward() {
		// TODO Auto-generated method stub
		return rewards;
	}

	public List<RewardItem.Builder> getAdvancedRewardList() {
		return advancedRewardList;
	}

	
	
	
}
