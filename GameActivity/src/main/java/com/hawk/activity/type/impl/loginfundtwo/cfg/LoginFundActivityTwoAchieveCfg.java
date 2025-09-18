package com.hawk.activity.type.impl.loginfundtwo.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import java.util.List;

/**
 * 登录基金活动配置
 * @author hf
 *
 */
@HawkConfigManager.XmlResource(file = "activity/login_fund_two/%s/loginfund_two_activity.xml", autoLoad=false, loadParams="285")
public class LoginFundActivityTwoAchieveCfg extends AchieveConfig {

	/** 成就id */
	@Id
	private final int achieveId;

	/** 成就条件类型 */
	private final int conditionType;

	/** 成就条件值 */
	private final String conditionValue;

	/** 奖励列表(通用奖励格式) */
	private final String rewards;
	/**对应的基金类型*/
	private final int fundType;

	private AchieveType achieveType;

	private List<RewardItem.Builder> rewardList;

	private List<Integer> conditionValueList;

	public LoginFundActivityTwoAchieveCfg() {
		achieveId = 0;
		conditionType = 0;
		conditionValue = "";
		rewards = "";
		fundType = 0;
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

	public int getFundType() {
		return fundType;
	}
}
