package com.hawk.game.module.mechacore.cfg;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRand;

import com.hawk.serialize.string.SerializeHelper;

/**
 * 机甲核心的模块词条库表
 * @author lating
 */
@HawkConfigManager.XmlResource(file = "xml/mecha_core_module_additional.xml")
public class MechaCoreModuleAddtionalCfg extends HawkConfigBase {

	@Id
	protected final int id;
	
	/**
	 * 品质（1.白色2.绿色3.蓝色4.紫色5.橙色6.红色)
	 */
	protected final int quality;
	/**
	 * 属性
	 */
	protected final String attr;
	/**
	 * 传承消耗 
	 */
	protected final String inheritConsume;
	/**
	 * 随机权重
	 */
	protected final int weight;
	
	/**
	 * 战斗力
	 */
	protected final int power;
	
	protected final int qualitySort;
	/**
	 * 对应兵种
	 */
	protected final String troopsTypes;
	protected final int effectGroup;
	
	
	/**
     * 强度配置
     */
    protected final String atkAttr;
    protected final String hpAttr;
	
	
	private int effect;
	private int randMin;
	private int randMax;
	private Set<Integer> troopsTypeSet = new HashSet<>();
	static Map<Integer, Map<Integer, Integer>> qualityCfgMap = new HashMap<>();
	
	public MechaCoreModuleAddtionalCfg() {
		id = 0;
		quality = 0;
		attr = "";
		inheritConsume = "";
		weight = 0;
		power = 0;
		qualitySort = 1;
		troopsTypes = "";
		effectGroup = 0;
		atkAttr = "";
		hpAttr = "";
	}
	
	@Override
	protected boolean assemble() {
		String[] array = attr.split("_");
		if (array.length < 3) {
			return false;
		}
		effect = Integer.parseInt(array[0]);
		randMin = Integer.parseInt(array[1]);
		randMax = Integer.parseInt(array[2]);
		
		Map<Integer, Integer> map = qualityCfgMap.get(quality);
		if (map == null) {
			map = new HashMap<>();
			qualityCfgMap.put(quality, map);
		}
		map.put(id, weight);
		troopsTypeSet = SerializeHelper.stringToSet(Integer.class, troopsTypes, ",");
		return true;
	}

	public int getId() {
		return id;
	}

	public int getQuality() {
		return quality;
	}

	public String getAttr() {
		return attr;
	}

	public String getInheritConsume() {
		return inheritConsume;
	}

	public int getWeight() {
		return weight;
	}

	public int getPower() {
		return power;
	}

	public int getEffect() {
		return effect;
	}

	public int getRandMin() {
		return randMin;
	}

	public int getRandMax() {
		return randMax;
	}
	
	public int getQualitySort() {
		return qualitySort;
	}
	
	public int getEffectGroup() {
		return effectGroup;
	}
	
	public Set<Integer> getTroopsTypeSet() {
		return troopsTypeSet;
	}


	public int getAtkAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(atkAttr).getOrDefault(soldierType, 0);
	}

    public int getHpAttr(int soldierType) {
        return SerializeHelper.cfgStr2Map(hpAttr).getOrDefault(soldierType, 0);
    }
    
    
	public static MechaCoreModuleAddtionalCfg randCfgByQuality(int quality) {
		Map<Integer, Integer> map = qualityCfgMap.get(quality);
		if (map == null || map.isEmpty()) {
			return null;
		}
		int cfgId = HawkRand.randomWeightObject(map);
		return HawkConfigManager.getInstance().getConfigByKey(MechaCoreModuleAddtionalCfg.class, cfgId);
	}
	
	public static Set<Integer> getCfgIdByQuality(int quality) {
		Map<Integer, Integer> map = qualityCfgMap.get(quality);
		if (map == null) {
			return Collections.emptySet();
		}
		
		return map.keySet();
	}
	
}
