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
 * 酒馆宝箱积分配置
 * @author PhilChen
 *
 */
@HawkConfigManager.XmlResource(file = "xml/tavern_score.xml")
public class TavernScoreCfg extends AchieveConfig {
	
	/** 成就id*/
	@Id
	private final int achieveId;
	/** 成就条件类型*/
	private final int conditionType;
	/** 成就条件值*/
	private final String conditionValue;
	/** 宝箱id*/
	private final int boxId;

	private AchieveType achieveType;
	private List<Integer> conditionValueList;
	
	public TavernScoreCfg() {
		achieveId = 0;
		conditionType = 0;
		conditionValue = "";
		boxId = 0;
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
	
	public int getBoxId() {
		return boxId;
	}

	@Override
	public String getReward() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
