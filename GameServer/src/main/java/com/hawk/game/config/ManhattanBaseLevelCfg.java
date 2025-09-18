package com.hawk.game.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.serialize.string.SerializeHelper;

/**
 * 超武基座等级
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/manhattan_base_level.xml")
public class ManhattanBaseLevelCfg extends HawkConfigBase {

	/**
	 * id
	 */
	@Id
	protected final int id;
	
	/**
	 * 部位id
	 */
	protected final int posId;
	
	/**
	 * 等级
	 */
	protected final int level;
	
	/**
	 * 升级消耗
	 */
	protected final String consumption;
	
	/**
	 * 战力
	 */
	protected final int power;
	
	/**
	 * 属性
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
	 * 部件每一等级对应的配置<posId*1000+level, config>
	 */
	private static Map<Long, ManhattanBaseLevelCfg> posLevelCfgMap = new HashMap<>();
	private static Set<Integer> posSet = new HashSet<>();
	
	public ManhattanBaseLevelCfg() {
		id = 0;
		posId = 0;
		level = 0;
		consumption = "";
		power = 0;
		attr = "";
		atkAttr = "";
		hpAttr = "";
	}

	public int getLevel() {
		return level;
	}

	public int getId() {
		return id;
	}

	public int getPosId() {
		return posId;
	}

	public String getConsumption() {
		return consumption;
	}

	public int getPower() {
		return power;
	}

	public String getAttr() {
		return attr;
	}
	
	public Map<Integer, Integer> getAttrMap() {
		return attrMap;
	} 

	@Override
	protected boolean assemble() {
		attrMap = SerializeHelper.stringToMap(attr, Integer.class, Integer.class, "_", ",");
		long key = getMapKey(posId, level);
		posSet.add(posId);
		posLevelCfgMap.put(key, this);
		return true;
	}
	
	
	
	public int getAtkAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(atkAttr).getOrDefault(soldierType, 0);
	}
	
	public int getHpAttr(int soldierType) {
	   return SerializeHelper.cfgStr2Map(hpAttr).getOrDefault(soldierType, 0);
	}
	
	public static Set<Integer> getPosSet() {
		return posSet;
	}
	
	private static long getMapKey(int posId, int level) {
		return posId * 1000L + level;
	}
	
	
	
	public static ManhattanBaseLevelCfg getConfig(int posId, int level) {
		long key = getMapKey(posId, level);
		return posLevelCfgMap.get(key);
	}
}
