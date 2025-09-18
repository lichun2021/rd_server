package com.hawk.activity.type.impl.allianceCarnival.cfg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;

import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 联盟总动员任务配置
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "activity/alliance_carnival/alliance_carnival_achieve.xml")
public class AllianceCarnivalAchieveCfg extends AchieveConfig {

	/**
	 * 任务id
	 */
	@Id
	private final int achieveId;
	
	/**
	 * 任务类型
	 */
	private final int conditionType;
	
	/**
	 * 条件值 
	 */
	private final String conditionValue;
	
	/**
	 * 每次完成奖励经验
	 */
	private final int exp;
	
	/**
	 * 刷新延迟(s)
	 */
	private final long refreshDelay;
	
	/**
	 * 时间限制(s)
	 */
	private final long timeLimit;
	
	/**
	 * 刷新权重
	 */
	private final int refreshWeight;

	/**
	 * 大本等级限制(这个字段暂时用不上)
	 */
	private final int levelLimit;
	
	/**
	 * 成就类型
	 */
	private AchieveType achieveType;
	
	private List<Integer> conditionValueList;
	
	public AllianceCarnivalAchieveCfg() {
		achieveId = 0;
		conditionType = 0;
		conditionValue = "";
		exp = 0;
		timeLimit = 30;
		refreshDelay = 0;
		refreshWeight = 1;
		levelLimit = 0;
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

	public int getExp() {
		return exp;
	}

	public long getRefreshDelay() {
		return refreshDelay * 1000L;
	}

	public long getTimeLimit() {
		return timeLimit * 1000L;
	}

	public int getRefreshWeight() {
		return refreshWeight;
	}

	public AchieveType getAchieveType() {
		return achieveType;
	}

	public int getLevelLimit() {
		return levelLimit;
	}

	@Override
	public List<Integer> getConditionValues() {
		return conditionValueList;
	}

	public String getReward(){
		return "";
	}
	
	public List<RewardItem.Builder> getRewardList(){
		return new ArrayList<>();
		
	}
	
	@Override
	protected boolean assemble() {
		
		achieveType = AchieveType.getType(conditionType);
		if (achieveType == null) {
			HawkLog.errPrintln("achieve type not found! type: {}", conditionType);
			return false;
		}
		
		conditionValueList = SerializeHelper.stringToList(Integer.class, conditionValue, SerializeHelper.ATTRIBUTE_SPLIT);
		
		return true;
	}

}