package com.hawk.activity.type.impl.resourceDefense.cfg;

import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

/**
 * 资源保卫战资源站配置
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "activity/resource_defense/resource_defense_station.xml")
public class ResourceDefenseStationCfg extends HawkConfigBase {

	@Id
	private final int id;

	/**
	 * 资源站类型
	 */
	private final int stationType;
	
	/**
	 * 资源站等级
	 */
	private final int level;
	
	/**
	 * 多久可以收获一次(s)
	 */
	private final int peroid;
	
	/**
	 * 收获次数
	 */
	private final int times;

	/**
	 * 单次奖励 晶体id_权重，晶体id_权重
	 */
	private final String reward;
	
	/**
	 * 加速值(万分比)
	 */
	private final int speedUp;

	/**
	 * 收取经验
	 */
	private final int collectExp;
	
	/**
	 * 偷取经验
	 */
	private final int pickupExp;
	
	/**
	 * 单次奖励Map
	 */
	private Map<Integer, Integer> rewardMap;
	
	/**
	 * 构造
	 */
	public ResourceDefenseStationCfg(){
		id = 0;
		stationType = 0;
		level = 0;
		peroid = 0;
		times = 0;
		reward = "";
		speedUp = 0;
		collectExp = 0;
		pickupExp = 0;
	}

	public int getId() {
		return id;
	}

	public int getStationType() {
		return stationType;
	}

	public int getLevel() {
		return level;
	}

	public long getPeroid() {
		return peroid * 1000L;
	}

	public int getTimes() {
		return times;
	}
	
	public Map<Integer, Integer> getRewardMap() {
		return rewardMap;
	}

	public int getSpeedUp() {
		return speedUp;
	}

	public int getCollectExp() {
		return collectExp;
	}

	public int getPickupExp() {
		return pickupExp;
	}

	@Override
	protected boolean assemble() {
		Map<Integer, Integer> rewardMap = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(reward)) {
			String[] single = reward.split(",");
			for (int i = 0; i < single.length; i++) {
				String[] info = single[i].split("_");
				rewardMap.put(Integer.valueOf(info[0]), Integer.valueOf(info[1]));
			}
		}
		this.rewardMap = rewardMap;
		
		return true;
	}
}