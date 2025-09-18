package com.hawk.activity.type.impl.questTreasure.cfg;

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
																	
@HawkConfigManager.XmlResource(file = "activity/quest_treasure/quest_treasure_achieve.xml")
public class QuestTreasureAchieveCfg extends AchieveConfig {
	
	public static final int BOX_ACHIEVE_TYPE = 1;  //宝箱任务
	public static final int DAILY_ACHIEVE_TYPE = 2; //每日任务
	public static final int WEEK_ACHIEVE_TYPE = 3;  //
	
	
	/** 成就id */
	@Id
	private final int achieveId;
	
	private final int type;

	/** 条件类型 */
	private final int conditionType;

	/** 条件值 */
	private final String conditionValue;

	/** 奖励列表 */
	private final String rewards;

	
	private final int refreshDays;

	private AchieveType achieveType;
	
	private List<RewardItem.Builder> rewardList;

	private List<Integer> conditionValueList;
	
	private static Map<Integer,Integer> refreshMap = new HashMap<>();

	public QuestTreasureAchieveCfg() {
		achieveId = 0;
		type = 0;
		conditionType = 0;
		conditionValue = "";
		rewards = "";
		refreshDays = 0;
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
			
			if(this.type !=BOX_ACHIEVE_TYPE && this.type != DAILY_ACHIEVE_TYPE && this.type != WEEK_ACHIEVE_TYPE ){
				HawkLog.errPrintln("achieve type not found! type: {}", type);
				return false;
			}
			if(this.refreshDays <= 0){
				HawkLog.errPrintln("achieve refreshDays err! refreshDays: {}", refreshDays);
				return false;
			}
			
			if(refreshMap.containsKey(this.type)){
				int rd = refreshMap.getOrDefault(this.type, 0);
				if(this.refreshDays != rd){
					HawkLog.errPrintln("achieve refreshDays deff! type: {},refreshDay:{}",this.type,refreshDays);
					return false;
				}
			}else{
				refreshMap.put(this.type, this.refreshDays);
			}
			
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
	
	
	public int getRefreshDays() {
		return refreshDays;
	}
}
