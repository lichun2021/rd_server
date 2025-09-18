package com.hawk.activity.type.impl.plantweapon.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRand;

@HawkConfigManager.XmlResource(file = "activity/plant_weapon/plant_weapon_reward.xml")
public class PlantWeaponRewardCfg extends HawkConfigBase {
	@Id
	private final int id;
	
	private final int highPool;
	
	private final int lowPool;
	
	private final int raffleTimes;
	/**
	 * 折扣信息
	 */
	private final String discountWeight;
	/**
	 * 购买所需花费
	 */
	private final String costItem;
	
	private List<Integer> discountList = new ArrayList<>();
	private List<Integer> weightList = new ArrayList<>();
	
	private static Map<Integer, PlantWeaponRewardCfg> configMap = new HashMap<>();
	
	public PlantWeaponRewardCfg(){
		this.id = 0;
		this.highPool = 0;
		this.lowPool = 0;
		this.raffleTimes = 0;
		this.discountWeight = "";
		this.costItem = "";
	}
	
	public boolean assemble() {
		configMap.put(raffleTimes, this);
		String[] discountArray = discountWeight.split(",");
		for (String discountItem : discountArray) {
			String[] arr = discountItem.split("_");
			discountList.add(Integer.valueOf(arr[0]));
			weightList.add(Integer.valueOf(arr[1]));
		}
		return true;
	}
	
	public int getId() {
		return id;
	}

	public int getHighPool() {
		return highPool;
	}

	public int getLowPool() {
		return lowPool;
	}

	public String getDiscountWeight() {
		return discountWeight;
	}
	
	public String getCostItem() {
		return costItem;
	}

	public int randomDisCount() {
		int discount = HawkRand.randomWeightObject(discountList, weightList);
		return discount;
	}
	
	public static PlantWeaponRewardCfg getConfig(int raffleTimes) {
		return configMap.get(raffleTimes);
	}
}
