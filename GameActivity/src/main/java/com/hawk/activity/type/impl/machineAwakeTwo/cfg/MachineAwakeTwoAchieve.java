package com.hawk.activity.type.impl.machineAwakeTwo.cfg;

import java.util.List;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.machineAwakeTwo.MachineAwakeTwoActivity;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.XmlResource(file = "activity/machine_awake_two/machine_awake_two_achieve.xml")
public class MachineAwakeTwoAchieve extends AchieveConfig {
	/** 成就id */
	@Id
	private final int achieveId;

	/** 条件类型 */
	private final int conditionType;

	/** 条件值 */
	private final String conditionValue;

	/** 奖励列表 */
	private final String rewards;
	
	private final String ghostRewards;
	
	private AchieveType achieveType;

	private List<RewardItem.Builder> rewardList;
	
	private List<RewardItem.Builder> ghostRewardList;

	private List<Integer> conditionValueList;

	public MachineAwakeTwoAchieve() {
		achieveId = 0;
		conditionType = 0;
		conditionValue = "";
		rewards = "";
		ghostRewards = "";
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
			ghostRewardList = RewardHelper.toRewardItemImmutableList(ghostRewards);
			
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
		ActivityManager instance = ActivityManager.getInstance();
		if (instance == null) {
			return rewardList;
		}
		
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(ActivityType.MACHINE_AWAKE_TWO_VALUE);
		if(opActivity.isPresent()){
			MachineAwakeTwoActivity activity = (MachineAwakeTwoActivity)opActivity.get();
			if (activity.isGhost()) {
				return ghostRewardList;
			}
		}
		
		return rewardList;
	}

	@Override
	public String getReward() {
		ActivityManager instance = ActivityManager.getInstance();
		if (instance == null) {
			return rewards;
		}
		
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(ActivityType.MACHINE_AWAKE_TWO_VALUE);
		if(opActivity.isPresent()){
			MachineAwakeTwoActivity activity = (MachineAwakeTwoActivity)opActivity.get();
			if (activity.isGhost()) {
				return ghostRewards;
			}
		}
		
		return rewards;
	}
}