package com.hawk.activity.type.impl.plantweapon.cfg;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRand;
import com.hawk.gamelib.activity.ConfigChecker;

@HawkConfigManager.XmlResource(file = "activity/plant_weapon/plant_weapon_pool.xml")
public class PlantWeaponPoolCfg extends HawkConfigBase {
	@Id
	private final int id;
	
	private final int pool;
	
	private final int raffleTimes;
	
	private final String reward;
	
	private List<String> awardList = new ArrayList<>();
	private List<Integer> weightList = new ArrayList<>();
	
	private static Map<Integer, PlantWeaponPoolCfg> lowPoolMap = new HashMap<>();
	private static Map<Integer, PlantWeaponPoolCfg> highPoolMap = new HashMap<>();
	
	public PlantWeaponPoolCfg(){
		this.id = 0;
		this.pool = 0;
		this.raffleTimes = 0;
		this.reward = "";
	}
	
	public boolean assemble() {
		if (pool == 1) {
			lowPoolMap.put(raffleTimes, this);
		} else {
			highPoolMap.put(raffleTimes, this);
		}
		
		String[] awrdItemArray = reward.split(",");
		for (String awardItem : awrdItemArray) {
			String[] arr = awardItem.split("_");
			awardList.add(String.format("%s_%s_%s", arr[0], arr[1], arr[2]));
			weightList.add(Integer.valueOf(arr[3]));
		}
		
		return true;
	}
	
	@Override
	protected final boolean checkValid() {
		for (String award : awardList) {
			boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(award);
			if (!valid) {
				throw new InvalidParameterException(String.format("PlantWeaponPoolCfg reward error, id: %s , award: %s", id, award));
			}
		}
		return super.checkValid();
	}

	public int getId() {
		return id;
	}

	public String getReward() {
		return reward;
	}

	public int getPool() {
		return pool;
	}

	public int getRaffleTimes() {
		return raffleTimes;
	}

	public static PlantWeaponPoolCfg getLowPoolConfig(int raffleTimes) {
		return lowPoolMap.get(raffleTimes);
	}
	
	public static PlantWeaponPoolCfg getHighPoolConfig(int raffleTimes) {
		return highPoolMap.get(raffleTimes);
	}
	
	public String randomAward() {
		String award = HawkRand.randomWeightObject(awardList, weightList);
		return award;
	}
}
