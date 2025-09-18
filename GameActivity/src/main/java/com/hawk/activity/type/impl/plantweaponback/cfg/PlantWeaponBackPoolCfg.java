package com.hawk.activity.type.impl.plantweaponback.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRand;

import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.XmlResource(file = "activity/plant_weapon_back/plant_weapon_back_pool.xml")
public class PlantWeaponBackPoolCfg extends HawkConfigBase {
	@Id
	private final int id;
	/**
	 * 奖池等级（后端不用）
	 */
	private final int type;
	/**
	 * 奖励内容
	 */
	private final String rewards;
	/**
	 * 替换奖励（当玩家超武已解锁，或解锁超武的碎片数够解锁时，再抽到超武碎片，就不给玩家发超武碎片奖励，而是直接发这个替换奖励）
	 */
	private final String replaceRewards;
	
	/**
	 * 权重
	 */
	private final String weights;
	
	private static List<Integer> rewardList = new ArrayList<>();
	private static Map<Integer, List<Integer>> buffWeightsMap = new HashMap<>();
	
	public PlantWeaponBackPoolCfg(){
		this.id = 0;
		this.type = 0;
		this.rewards = "";
		this.replaceRewards = "";
		this.weights = "";
	}
	
	public boolean assemble() {
		rewardList.add(id);
		Map<Integer, Integer> map = SerializeHelper.stringToMap(weights, Integer.class, Integer.class, "_", ",");
		for (Entry<Integer, Integer> entry : map.entrySet()) {
			int buff = entry.getKey();
			List<Integer> list = buffWeightsMap.get(buff);
			if (list == null) {
				list = new ArrayList<>();
				buffWeightsMap.put(buff, list);
			}
			list.add(entry.getValue());
		}
		return true;
	}
	
	@Override
	protected final boolean checkValid() {
		return super.checkValid();
	}

	public int getId() {
		return id;
	}

	public int getType() {
		return type;
	}

	public String getRewards() {
		return rewards;
	}

	public String getWeights() {
		return weights;
	}

	public static PlantWeaponBackPoolCfg randomRewardCfg(int buff) {
		List<Integer> weightList = buffWeightsMap.get(buff);
		int cfgId = HawkRand.randomWeightObject(rewardList, weightList);
		return HawkConfigManager.getInstance().getConfigByKey(PlantWeaponBackPoolCfg.class, cfgId);
	}

	public String getReplaceRewards() {
		return replaceRewards;
	}
}
