package com.hawk.activity.type.impl.goldBabyNew.cfg;

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
 * 
 * @author fwj
 * 成就奖励类
 * 分为每日重置成就和累计成就
 */
@HawkConfigManager.XmlResource(file = "activity/gold_baby_new/%s/gold_baby_new_achieve.xml", autoLoad=false, loadParams="332")
public class GoldBabyNewDailyAchieveCfg extends AchieveConfig {
	/** 成就id*/
	@Id
	private final int achieveId;
	/** 条件类型*/
	private final int conditionType;
	/** 条件值*/
	private final String conditionValue;
	/** 奖励列表*/
	private final String rewards;
	/** 是否每日重置*/
	private final int resetting;

	private AchieveType achieveType;
	
	private List<RewardItem.Builder> rewardList;
	
	private List<Integer> conditionValueList;

	public GoldBabyNewDailyAchieveCfg() {
		achieveId = 0;
		conditionType = 0;
		resetting=0;
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


	public AchieveType getAchieveType() {
		return achieveType;
	}


	public void setAchieveType(AchieveType achieveType) {
		this.achieveType = achieveType;
	}


	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}


	public void setRewardList(List<RewardItem.Builder> rewardList) {
		this.rewardList = rewardList;
	}


	public List<Integer> getConditionValueList() {
		return conditionValueList;
	}


	public void setConditionValueList(List<Integer> conditionValueList) {
		this.conditionValueList = conditionValueList;
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


	public String getRewards() {
		return rewards;
	}


	public int getResetting() {
		return resetting;
	}


	@Override
	public List<Integer> getConditionValues() {
		return conditionValueList;
	}


	@Override
	public String getReward() {
		return rewards;
	}


	@Override
	public int getConditionValue(int index) {
		return conditionValueList.get(index);
	}
	
}
