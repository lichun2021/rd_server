
package com.hawk.activity.type.impl.dailysign.cfg;

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
 * 月签成就奖励
 * @author RickMei 
 *
 */
@HawkConfigManager.XmlResource(file = "activity/daily_sign/daily_sign_cumulative.xml")
public class DailySignAchieveCfg extends AchieveConfig {
	/** 成就id*/
	@Id
	private final int achieveId;
	/** 条件类型*/
	private final int conditionType;
	/** 条件值*/
	private final String conditionValue;
	/** 奖励列表*/
	private final String rewards;
	
	/**累签term**/
	private final int pool;
	
	private static int maxTermId = 0;
	private AchieveType achieveType;
	private List<RewardItem.Builder> rewardList;
	private List<Integer> conditionValueList;

	public DailySignAchieveCfg() {
		achieveId = 0;
		conditionType = 0;
		conditionValue = "";
		rewards = "";
		pool = 0;
	}
	
	@Override
	protected boolean assemble() {
		try {
			maxTermId = Math.max(maxTermId , this.pool);
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
	
	public static int getMaxTermId() {
		return maxTermId;
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

	public int getPool() {
		return pool;
	}
	
}
