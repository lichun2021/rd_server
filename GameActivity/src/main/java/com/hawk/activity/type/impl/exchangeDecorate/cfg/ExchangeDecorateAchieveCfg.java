package com.hawk.activity.type.impl.exchangeDecorate.cfg;

import java.util.List;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.serialize.string.SerializeHelper;


/**
 * 成就配置
 * @author luke
 */
@HawkConfigManager.XmlResource(file = "activity/freeavatar/freeavatar_achieve.xml")
public class ExchangeDecorateAchieveCfg extends AchieveConfig {
	/** 成就id */
	@Id
	private final int achieveId;
	/**
	 * 第X周触发数据
	 */
	private final int cycle;
	/** 条件类型 */
	private final int conditionType;
	/** 条件值 */
	private final String conditionValue;
	//经验
	private final int exp;
	// 1=每天刷新 2=每周刷新
	private final int questType;
	
	private AchieveType achieveType;
	private List<Integer> conditionValueList;

	public ExchangeDecorateAchieveCfg() {
		achieveId = 0;
		cycle=0;
		conditionType = 0;
		conditionValue = "";
		exp=0;
		questType=0;
	}
	public static int maxCycle = 0;
	@Override
	protected boolean assemble() {
		try {
			if(cycle>maxCycle){
				maxCycle = cycle;
			}
			
			achieveType = AchieveType.getType(conditionType);
			if (achieveType == null) {
				HawkLog.errPrintln("achieve type not found! type: {}", conditionType);
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
		return null;
	}

	@Override
	public String getReward() {
		return null;
	}

	public List<Integer> getConditionValueList() {
		return conditionValueList;
	}

	public void setConditionValueList(List<Integer> conditionValueList) {
		this.conditionValueList = conditionValueList;
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

	public boolean isQuestDay(){
		return questType == 1?true:false;
	}
	
	public int getQuestType() {
		return questType;
	}

	public void setAchieveType(AchieveType achieveType) {
		this.achieveType = achieveType;
	}

	public int getCycle() {
		return cycle;
	}

}
