package com.hawk.activity.type.impl.dividegold.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.serialize.string.SerializeHelper;

/**瓜分金币成就数据
 * @author Winder
 */
@HawkConfigManager.XmlResource(file = "activity/divide_gold/dividegold_achieve.xml")
public class DivideGoldAchieveCfg extends AchieveConfig{
	/** 成就id */
	@Id
	private final int achieveId;
	/** 条件类型 */
	private final int conditionType;
	/** 条件值 */
	private final String conditionValue;
	/** 奖励列表 */
	private final String rewards;
	//第几阶段刷新用的
	private final int round;
	
	private AchieveType achieveType;
	private List<RewardItem.Builder> rewardList;
	private List<Integer> conditionValueList;

	private static Map<Integer, List<Integer>> roundAchieveMap;
	
	public DivideGoldAchieveCfg() {
		achieveId = 0;
		conditionType = 0;
		conditionValue = "";
		rewards = "";
		round = 0;
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
			
			if (roundAchieveMap == null) {
				roundAchieveMap = new HashMap<>();
			}
			if (roundAchieveMap.containsKey(round)) {
				List<Integer> achieveIdList = roundAchieveMap.get(round);
				if (!achieveIdList.contains(achieveId)) {
					achieveIdList.add(achieveId);
				}
			}else{
				List<Integer> achieveIdList  = new ArrayList<>();
				achieveIdList.add(achieveId);
				roundAchieveMap.put(round, achieveIdList);
			}
			if (roundAchieveMap == null) {
				HawkLog.errPrintln("DivideGoldAchieveCfg faild ");
				return false;
			}
			
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

	public int getRound() {
		return round;
	}

}
