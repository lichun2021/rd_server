package com.hawk.activity.type.impl.skinPlan.cfg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
/**
 * 皮肤计划活动配置
 * @author Winder
 */
@HawkConfigManager.XmlResource(file = "activity/hero_skin/hero_skin_pool.xml")
public class SkinPlanActivityPoolCfg extends HawkConfigBase{
	@Id
	private final int id;
	
	private final int pool;
	
	private final String item;
	
	private final int weight;
	
	private static Map<Integer, Map<Integer, Integer>> weightMap;
	
	private List<RewardItem.Builder> rewardList;
	
	public SkinPlanActivityPoolCfg(){
		this.id = 0;
		this.pool = 0;
		this.item = "";
		this.weight = 0;
	}
	
	@Override
	protected boolean assemble() {
		rewardList = RewardHelper.toRewardItemImmutableList(item);
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
			HawkLog.errPrintln("SkinPlanActivityPoolCfg faild ");
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


	public String getItem() {
		return item;
	}


	public int getWeight() {
		return weight;
	}

	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}
	
}
