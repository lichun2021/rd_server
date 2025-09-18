package com.hawk.activity.type.impl.plantweapon.cfg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.KVResource(file = "activity/plant_weapon/plant_weapon_cfg.xml")
public class PlantWeaponKVCfg extends HawkConfigBase {
	/**
	 * 服务器开服延时开启活动时间；单位：秒
	 */
	private final long serverDelay;
	/**
	 * 当期超武解锁道具
	 */
	private final String unlockPlantWeapon;
	/**
	 * 当期超武id
	 */
	private final int PlantWeapon;
	/**
	 * 加1计数道具
	 */
	private final int addOneItem;
	/**
	 * 加3计数道具
	 */
	private final int addThreeItem;
	/**
	 * 单轮最大连续研究次数
	 */
	private final int maxExtract;
	/**
	 * 连续放弃次数_等待时间
	 */
	private final String continuityWaive;
	/**
	 * 必给加1道具的 累计消耗材料数
	 */
	private final int needItemValue;
	
	private final int turnMax; //轮次上限
	private final int inspirationMax; //灵感值上限
	private final String unlockPlantWeapons; //往期投放的超武可选列表，格式：奖励三段式_超武id,奖励三段式_超武id,....
	//每次研究的额外奖励
	private final String extReward;
	
	private Map<Integer, String> plantWeaponUnlockItemMap = new HashMap<>();
	private List<Integer> continuityWaiveParams;
	
	public PlantWeaponKVCfg(){
		this.serverDelay = 0;
		this.unlockPlantWeapon = "";
		this.PlantWeapon = 0;
		this.addOneItem = 0;
		this.addThreeItem = 0;
		this.maxExtract = 0;
		this.continuityWaive = "";
		this.needItemValue = 0;
		this.turnMax = 1;
		this.inspirationMax = 3;
		this.unlockPlantWeapons = "";
		this.extReward = "";
	}
	
	public boolean assemble() {
		if (PlantWeapon <= 0) {
			return false;
		}
		continuityWaiveParams = SerializeHelper.stringToList(Integer.class, continuityWaive, "_");
		if (continuityWaiveParams.isEmpty() || continuityWaiveParams.size() < 2) {
			return false;
		}
		List<String> unlockPlantWeaponChooseList = SerializeHelper.stringToList(String.class, unlockPlantWeapons, ",");
		plantWeaponUnlockItemMap.put(PlantWeapon, unlockPlantWeapon);
		for (String chooseUnlock : unlockPlantWeaponChooseList) {
			String[] arr = chooseUnlock.split("_");
			if (arr.length < 4) {
				return false;
			}
			int swId = Integer.valueOf(arr[3]);
			plantWeaponUnlockItemMap.put(swId, String.format("%s_%s_%s", arr[0], arr[1], arr[2]));
		}
		
		return true;
	}

	public long getServerDelay() {
		return serverDelay * 1000L;
	}

	public String getUnlockPlantWeapon() {
		return unlockPlantWeapon;
	}
	
	public int getPlantWeapon() {
		return PlantWeapon;
	}

	public int getAddOneItem() {
		return addOneItem;
	}

	public int getAddThreeItem() {
		return addThreeItem;
	}

	public int getMaxExtract() {
		return maxExtract;
	}

	public String getContinuityWaive() {
		return continuityWaive;
	}

	public int getNeedItemValue() {
		return needItemValue;
	}

	public int getContinuityWaiveTimes() {
		return continuityWaiveParams.get(0);
	}
	
	public long getContinuityWaiveCoolDownTime() {
		return continuityWaiveParams.get(1) * 1000L;
	}

	public int getTurnMaxVal() {
		return turnMax;
	}

	public int getInspireMaxVal() {
		return inspirationMax;
	}

	public String getUnlockPlantWeapons() {
		return unlockPlantWeapons;
	}

	public String getPlantWeaponUnlockItem(int swId) {
		return plantWeaponUnlockItemMap.get(swId);
	}
	
	public String getExtReward() {
		return extReward;
	}
}
