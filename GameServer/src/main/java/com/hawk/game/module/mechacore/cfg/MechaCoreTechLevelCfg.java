package com.hawk.game.module.mechacore.cfg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.serialize.string.SerializeHelper;

/**
 * 机甲核心的科技升级表
 * @author lating
 */
@HawkConfigManager.XmlResource(file = "xml/mecha_core_technology_level.xml")
public class MechaCoreTechLevelCfg extends HawkConfigBase {

	@Id
	protected final int id;
	
	/**
	 * 核心科技类型 (1.材料科技 2.散热科技 3.计算科技)
	 */
	protected final int coreTechnologyLevelType;
	
	/**
	 * 科技等级
	 */
	protected final int coreLevels;
	
	/**
	 * 突破后的阶位等级
	 */
	protected final int rankLevel;
	
	/**
	 * 标记：1.初始等级 2.突破等级 3.最终等级
	 */
	protected final int specialLevel;
	
	/**
	 * 加成属性：升级后直接读取属性，不累加
	 */
	protected final String coreTechnologyLevelAttr;
	
	/**
	 * 战力展示：升级时直接展示战力数值，需要累加
	 */
	protected final int power;

	/**
	 * 需求材料：升到下一级需要的材料数量
	 */
	protected final String needItem;
	
	/**
     * 强度配置
     */
    protected final String atkAttr;
    protected final String hpAttr;
	
	/**
	 * 属性map
	 */
	private Map<Integer, Integer> attrMap = new HashMap<>();
	
	private static Set<Integer> typeTechs = new HashSet<>();
	
	private static Map<Integer, MechaCoreTechLevelCfg> typeLevelCfgMap = new HashMap<>();
	
	public MechaCoreTechLevelCfg() {
		id = 0;
		coreTechnologyLevelType = 0;
		coreLevels = 0;
		rankLevel = 0;
		specialLevel = 0;
		coreTechnologyLevelAttr = "";
		power = 0;
		needItem = "";
		atkAttr = "";
		hpAttr = "";
	}
	
	@Override
	protected boolean assemble() {
		typeTechs.add(coreTechnologyLevelType);
		typeLevelCfgMap.put(coreTechnologyLevelType * 10000 + coreLevels, this);
		attrMap = SerializeHelper.stringToMap(coreTechnologyLevelAttr, Integer.class, Integer.class, "_", ",");
		return true;
	}

	public Map<Integer, Integer> getAttrMap() {
		return attrMap;
	} 
	
	public int getId() {
		return id;
	}

	public int getPower() {
		return power;
	}

	public int getCoreTechType() {
		return coreTechnologyLevelType;
	}

	public int getCoreLevels() {
		return coreLevels;
	}

	public int getRankLevel() {
		return rankLevel;
	}

	public int getSpecialLevel() {
		return specialLevel;
	}

	public String getCoreTechLevelAttr() {
		return coreTechnologyLevelAttr;
	}

	public String getNeedItem() {
		return needItem;
	}

	public static Set<Integer> getTypeTechs() {
		return typeTechs;
	}
	
	public int getAtkAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(atkAttr).getOrDefault(soldierType, 0);
	}

    public int getHpAttr(int soldierType) {
        return SerializeHelper.cfgStr2Map(hpAttr).getOrDefault(soldierType, 0);
    }
	
	public static MechaCoreTechLevelCfg getCfgByLevel(int type, int level) {
		int key = type * 10000 + level;
		return typeLevelCfgMap.get(key);
	}
}
