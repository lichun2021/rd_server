package com.hawk.game.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 超武等级配置
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/manhattan_sw_level.xml")
public class ManhattanSWLevelCfg extends HawkConfigBase {

	/**
	 * 序列id
	 */
	@Id
	protected final int id;

	/**
	 * 对应超武
	 */
	protected final int swId;
	
	/**
	 * 对应部件
	 */
	protected final int posId;
	
	/**
	 * 前置底座品阶
	 */
	protected final int unlockBaseStage;

	/**
	 * 对应等级
	 */
	protected final int level;

	/**
	 * 升级消耗
	 */
	protected final String consumption;
	
	/**
	 * 消耗返还
	 */
	protected final String returnConsumption;

	/**
	 * 等级战力
	 */
	protected final int power;
	
	/**
	 * 等级属性
	 */
	protected final String attr;

   /**
	* 强度配置
	*/
	protected final String atkAttr;
	protected final String hpAttr;



	/**
	 * 属性map
	 */
	private Map<Integer, Integer> attrMap = new HashMap<>();
	
	/**
	 * 超武、部件每一等级对应的配置
	 */
	private static Table<Integer, Long, ManhattanSWLevelCfg> posLevelCfgTable = HashBasedTable.create();
	
	private static Map<Integer, Set<Integer>> swPosMap = new HashMap<>();
	
	public ManhattanSWLevelCfg() {
		id = 0;
		swId = 0;
		posId = 0;
		unlockBaseStage = 0;
		level = 0;
		consumption = "";
		returnConsumption = "";
		power = 0;
		attr = "";
		atkAttr = "";
		hpAttr = "";
	}
	
	public int getId() {
		return id;
	}

	public int getSwId() {
		return swId;
	}

	public int getPosId() {
		return posId;
	}

	public int getLevel() {
		return level;
	}

	public String getConsumption() {
		return consumption;
	}
	
	public String getReturnConsumption() {
		return returnConsumption;
	}

	public int getPower() {
		return power;
	}

	public String getAttr() {
		return attr;
	}
	
	public int getUnlockBaseStage() {
		return unlockBaseStage;
	}

	@Override
	protected boolean assemble() {
		attrMap = SerializeHelper.stringToMap(attr, Integer.class, Integer.class, "_", ",");
		Set<Integer> set = swPosMap.get(swId);
		if (set == null) {
			set = new HashSet<>();
			swPosMap.put(swId, set);
		}
		
		set.add(posId);
		long key = getMapKey(posId, level);
		posLevelCfgTable.put(swId, key, this);
		return true;
	}
	
	public Map<Integer, Integer> getAttrMap() {
		return attrMap;
	} 
	
	public int getAtkAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(atkAttr).getOrDefault(soldierType, 0);
	}

	public int getHpAttr(int soldierType) {
	   return SerializeHelper.cfgStr2Map(hpAttr).getOrDefault(soldierType, 0);
	}
	
	
	
	public static Set<Integer> getSwPos(int swId) {
		return swPosMap.getOrDefault(swId, Collections.emptySet());
	}
	
	private static long getMapKey(int posId, int level) {
		return posId * 1000L + level;
	}
	
	public static ManhattanSWLevelCfg getConfig(int swId, int posId, int level) {
		long key = getMapKey(posId, level);
		return posLevelCfgTable.get(swId, key);
	}

}
