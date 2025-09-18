package com.hawk.activity.type.impl.lotteryDraw.cfg;

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
 * 十连抽活动成就宝箱配置
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "activity/lottery_draw/lottery_draw_achieve.xml")
public class LotteryDrawAchieveCfg extends  AchieveConfig {
	/** */
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
	
	public LotteryDrawAchieveCfg() {
		achieveId = 0;
		rewards = "";
		conditionType = 0;
		conditionValue = "";
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
		return rewards;
	}
	
}
