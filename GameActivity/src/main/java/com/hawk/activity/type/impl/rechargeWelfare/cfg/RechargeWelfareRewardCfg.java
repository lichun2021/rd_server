package com.hawk.activity.type.impl.rechargeWelfare.cfg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
/**
 * 充值福利活动
 */
@HawkConfigManager.XmlResource(file = "activity/recharge_welfare/recharge_welfare_reward.xml")
public class RechargeWelfareRewardCfg extends HawkConfigBase{
	@Id
	private final int id;
	
	private final int pool;
	
	private final String reward;
	
	private final int weight;
	
	private final int defaultChoose;
	
	private static Map<Integer, Map<Integer, Integer>> weightMap;
	
	private List<RewardItem.Builder> rewardList;
	
	public RechargeWelfareRewardCfg(){
		this.id = 0;
		this.pool = 0;
		this.reward = "";
		this.weight = 0;
		this.defaultChoose = 0;
		
	}
	
	@Override
	protected boolean assemble() {
		rewardList = RewardHelper.toRewardItemImmutableList(reward);
		if (weightMap == null) {
			weightMap = new HashMap<>();
		}
		if (weightMap.containsKey(pool)) {
			Map<Integer,Integer> map = weightMap.get(pool);
			if (!map.containsKey(id)) {
				map.put(id, weight);
			}
		}else{
			Map<Integer,Integer> map = new HashMap<>();
			map.put(id, weight);
			weightMap.put(pool, map);
		}
		if (weightMap == null) {
			HawkLog.errPrintln("RechargeWelfareActivityPoolCfg faild ");
			return false;
		}
		return super.assemble();
	}

	public static Map<Integer, Integer> getWeightMapByLevel(int level) {
		return weightMap.get(level);
	}

	public int getId() {
		return id;
	}


	public int getPool() {
		return pool;
	}


	public String getReward() {
		return reward;
	}

	public int getWeight() {
		return weight;
	}

	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}

	public int getDefaultChoose() {
		return defaultChoose;
	}
	
}
