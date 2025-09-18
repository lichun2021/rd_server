package com.hawk.activity.type.impl.festivalTwo.cfg;

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
 * 八日盛典活动积分奖励配置
 * @author PhilChen
 *
 */
@HawkConfigManager.XmlResource(file = "activity/festival_two/festival_two_score.xml")
public class FestivalTwoScoreCfg extends AchieveConfig {
	/** */
	@Id
	private final int achieveId;
	
	/** 条件类型*/
	private final int conditionType;
	/** 条件值*/
	private final String conditionValue;
	
	/** 奖励列表*/
	private final String rewards;
	
	private final int isGainRewardDelay;
	
	
	private AchieveType achieveType;
	private List<RewardItem.Builder> rewardList;
	private List<Integer> conditionValueList;
	
	public FestivalTwoScoreCfg() {
		achieveId = 0;
		rewards = "";
		conditionType = 0;
		conditionValue = "";
		isGainRewardDelay = 0;
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
	
	public int getAchieveId() {
		return achieveId;
	}

	public String getRewards() {
		return rewards;
	}

	public int getIsGainRewardDelay() {
		return isGainRewardDelay;
	}

	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
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
	
}
