package com.hawk.activity.type.impl.groupPurchase.cfg;

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
 * 跨服团购活动配置
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "activity/group_purchase/%s/group_purchase_achieve.xml", autoLoad=false, loadParams="30")
public class GroupPurchaseAchieveCfg extends AchieveConfig {
	/** 成就id*/
	@Id
	private final int achieveId;
	/** 条件类型*/
	private final int conditionType;
	/** 条件值*/
	private final String conditionValue;
	/** 奖励列表*/
	private final String rewards;
	
	private AchieveType achieveType;
	private List<RewardItem.Builder> rewardList;
	private List<Integer> conditionValueList;

	public GroupPurchaseAchieveCfg() {
		achieveId = 0;
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
		// TODO Auto-generated method stub
		return rewards;
	}
	
}
