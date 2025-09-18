package com.hawk.game.config;

import java.util.Collections;
import java.util.List;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 酒馆成就配置
 * @author PhilChen
 *
 */
@HawkConfigManager.XmlResource(file = "xml/tavern_achieve.xml")
public class TavernAchieveCfg extends AchieveConfig {

	/** 成就id*/
	@Id
	private final int achieveId;
	/** 成就条件类型*/
	private final int conditionType;
	/** 成就条件值*/
	private final String conditionValue;
	/** 可完成次数*/
	private final int count;
	/** 增加积分*/
	private final int score;

	private AchieveType achieveType;
	private List<Integer> conditionValueList;
	
	public TavernAchieveCfg() {
		achieveId = 0;
		conditionType = 0;
		conditionValue = "";
		count = 0;
		score = 0;
	}

	@Override
	protected boolean assemble() {
		try {
			achieveType = AchieveType.getType(conditionType);
			if (achieveType == null) {
				HawkLog.errPrintln("achieve type not found! type={}", conditionType);
				return false;
			}
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
		return Collections.emptyList();
	}

	public int getScore() {
		return score;
	}
	
	public int getCount() {
		return count;
	}

	@Override
	public String getReward() {
		// TODO Auto-generated method stub
		return null;
	}
}
