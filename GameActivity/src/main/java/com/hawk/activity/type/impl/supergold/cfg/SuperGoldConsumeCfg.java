package com.hawk.activity.type.impl.supergold.cfg;

import java.util.List;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.game.protocol.Reward.RewardItem;

@HawkConfigManager.XmlResource(file = "activity/super_gold/super_gold_consumer.xml")
public class SuperGoldConsumeCfg extends AchieveConfig{
	
	/** 成就id*/
	@Id
	private final int achieveId;
	/** 条件类型*/
	private final int conditionType; //AchieveType
	/** 条件值*/
	private final int conditionValue; //需要达成的条件(上一个成就完成才达成)
	/** 消耗金条 **/
	private final String cost;
	/** 获得奖励 **/
	private final String rewards;
	
	private AchieveType achieveType;
	
	private List<RewardItem.Builder> needItemList;
	
	private int cost_gold;
	
	public SuperGoldConsumeCfg() {
		achieveId = 0;
		conditionType = 0;
		conditionValue = 0;
		cost = "";
		rewards = "";
		cost_gold = 0;
	}
	
	public boolean assemble() {
		try {
			achieveType = AchieveType.getType(conditionType);
			if (achieveType == null) {
				HawkLog.errPrintln("achieve type not found! type: {}", conditionType);
				return false;
			}
			this.needItemList = RewardHelper.toRewardItemImmutableList(this.cost);
			decodeCost_gold();
			return true;
		} catch (Exception arg1) {
			HawkException.catchException(arg1, new Object[0]);
			return false;
		}
	}

	public int getAchieveId() {
		return achieveId;
	}

	public int getConditionType() {
		return conditionType;
	}

	public String getReward() {
		return rewards;
	}

	public int getConditionValue() {
		return conditionValue;
	}

	public String getCost() {
		return cost;
	}

	public int getCost_gold() {
		return cost_gold;
	}

	/***
	 * 构建金条Builder
	 * @return
	 */
	public List<RewardItem.Builder> getNeedItemList() {
		return needItemList;
	}
	
	private void decodeCost_gold(){
		String[] items = cost.split("_");
		if (items.length < 3) {
			throw new RuntimeException("SuperGoldConsumeCfg Error: invalid cost :" + cost);
		}
		cost_gold = Integer.parseInt(items[2]);
	}

	/***
	 * 校验
	 * 1.不止校验是否成就id相同，还校验配置格式是否异常
	 */
	@Override
	public boolean existConfigId() {
		if(super.existConfigId()){
			return false;
		}
		try {
			List<RewardItem.Builder> checkList = RewardHelper.toRewardItemList(this.cost);
			if(checkList == null){
				throw new HawkException("SuperGoldConsumerCfg Error, invalid cost :" + cost);
			}
			List<RewardItem.Builder> checkReward = RewardHelper.toRewardItemList(this.rewards);
			if(checkReward == null){
				throw new HawkException("SuperGoldConsumerCfg Error, invalid reward :" + rewards);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
		
		return false;
	}
}
