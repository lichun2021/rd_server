package com.hawk.activity.type.impl.resourceDefense.cfg;

import com.hawk.game.protocol.Activity;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import java.util.HashMap;
import java.util.Map;

/**
 * 资源保卫机器人配置
 * hf
 */
@HawkConfigManager.XmlResource(file = "activity/resource_defense/resource_defense_robot.xml")
public class ResourceDefenseRobotCfg extends HawkConfigBase {

	@Id
	private final int id;
	private final int type;
	private final int level;
	private final String resourceInfo;
	private final int pickupWeight;
	private final int pickupExp;
	private final int refreshType;
	private final int refreshWeight;

	private final String showStationType;

	private Map<Integer, Integer> resourceInfoMap = new HashMap<>();

	private static Map<Integer, Map<Integer,Integer>> typeIdWeightMap = new HashMap<>();

	private Map<Integer, Integer> showStationTypeMap = new HashMap<>();

	private static int greatRobotId;

	public ResourceDefenseRobotCfg(){
		id = 0;
		type = 0;
		level = 0;
		resourceInfo = "";
		pickupWeight = 0;
		pickupExp = 0;
		refreshType = 0;
		refreshWeight = 0;
		showStationType = "";
	}
	@Override
	protected boolean assemble() {
		this.resourceInfoMap = SerializeHelper.stringToMap(resourceInfo, Integer.class, Integer.class, "_", ",");
		this.showStationTypeMap  = SerializeHelper.stringToMap(showStationType, Integer.class, Integer.class, "_", ",");
		Map<Integer, Integer> map = null;
		try {
			map = typeIdWeightMap.getOrDefault(type, new HashMap<>());
			map.put(id, refreshWeight);
			typeIdWeightMap.put(type, map);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}

		if (type == Activity.PBRobotType.ROBOT_3_VALUE){
			greatRobotId = id;
		}
		return true;
	}

	public int getId() {
		return id;
	}

	public int getType() {
		return type;
	}

	public int getLevel() {
		return level;
	}

	public String getResourceInfo() {
		return resourceInfo;
	}

	public int getPickupWeight() {
		return pickupWeight;
	}

	public int getPickupExp() {
		return pickupExp;
	}

	public int getRefreshType() {
		return refreshType;
	}


	public Map<Integer, Integer> getResourceInfoMap() {
		return resourceInfoMap;
	}

	public void setResourceInfoMap(Map<Integer, Integer> resourceInfoMap) {
		this.resourceInfoMap = resourceInfoMap;
	}

	public int getRefreshWeight() {
		return refreshWeight;
	}

	public static Map<Integer, Map<Integer, Integer>> getTypeIdWeightMap() {
		return typeIdWeightMap;
	}

	public static Map<Integer,Integer> getTypeIdWeightMapByType(int type) {
		return typeIdWeightMap.getOrDefault(type, new HashMap<>());
	}

	public static int getGreatRobotId() {
		return greatRobotId;
	}

	public Map<Integer, Integer> getShowStationTypeMap() {
		return showStationTypeMap;
	}

	public void setShowStationTypeMap(Map<Integer, Integer> showStationTypeMap) {
		this.showStationTypeMap = showStationTypeMap;
	}
}
