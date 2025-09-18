package com.hawk.activity.type.impl.recallFriend.cfg;


import java.security.InvalidParameterException;
import java.util.List;

import org.hawk.config.HawkConfigManager.XmlResource;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.serialize.string.SerializeHelper;

@XmlResource(file = "activity/recall_friend/recall_friend_task.xml")
public class RecallFriendTaskCfg extends AchieveConfig {
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
	 * 奖励
	 */
	private final String rewards;
	/**
	 * 成就是否需要重置
	 */
	private final int reset;
	/**
	 * 奖励解析
	 */
	private  List<RewardItem.Builder> rewardList;
	/**
	 * 条件解析
	 */
	private List<Integer> conditionValueList;
	private AchieveType achieveType;
	public RecallFriendTaskCfg() {
		this.achieveId = 0;
		this.conditionType = 0;
		this.conditionValue = "";
		this.rewards = "";
		this.reset = 0;
	}
	
	@Override
	public boolean assemble(){
		this.conditionValueList = SerializeHelper.stringToList(Integer.class, conditionValue, SerializeHelper.ATTRIBUTE_SPLIT);
		this.rewardList = RewardHelper.toRewardItemImmutableList(rewards);
		achieveType = AchieveType.getType(conditionType);
		
		return true;
	}
	
	@Override
	public boolean checkValid() {
		if (achieveType == null) {
			throw new InvalidParameterException(String.format("配置的成就类型找不到:%s", conditionType));
		}
		
		return true;
	}
	
	@Override
	public int getAchieveId() {
		return achieveId;
	}
	
	@Override
	public AchieveType getAchieveType () {
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

	public int getReset() {
		return reset;
	}
}
