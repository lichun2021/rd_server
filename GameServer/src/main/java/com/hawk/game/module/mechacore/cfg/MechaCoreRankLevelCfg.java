package com.hawk.game.module.mechacore.cfg;

import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.serialize.string.SerializeHelper;

/**
 * 机甲核心突破表
 * @author lating
 */
@HawkConfigManager.XmlResource(file = "xml/mecha_core_technology_rank.xml")
public class MechaCoreRankLevelCfg extends HawkConfigBase {

	@Id
	protected final int id;
	/**
	 * 核心突破等级
	 */
	protected final int coreRankLevel;
	
	/**
	 * 对应科技等级条件
	 */
	protected final int techLevelLimit;
	
	/**
	 * 加成属性
	 */
	protected final String coreRankAttr;
	
	/**
	 * 技能id：突破后给玩家附加的技能
	 */
	protected final String coreRankEffectId;
	
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
	private Map<Integer, Integer> skillAttrMap = new HashMap<>();
	
	private static Map<Integer, MechaCoreRankLevelCfg> levelCfgMap = new HashMap<>();
	
	public MechaCoreRankLevelCfg() {
		id = 0;
		coreRankLevel = 0;
		techLevelLimit = 0;
		coreRankAttr = "";
		coreRankEffectId = "";
		power = 0;
		needItem = "";
		atkAttr = "";
		hpAttr = "";
	}
	
	@Override
	protected boolean assemble() {
		levelCfgMap.put(coreRankLevel, this);
		attrMap = SerializeHelper.stringToMap(coreRankAttr, Integer.class, Integer.class, "_", ",");
		skillAttrMap = SerializeHelper.stringToMap(coreRankEffectId, Integer.class, Integer.class, "_", ",");
		return true;
	}
	
	public Map<Integer, Integer> getAttrMap() {
		return attrMap;
	} 
	
	public Map<Integer, Integer> getSkillAttrMap() {
		return skillAttrMap;
	} 

	public int getId() {
		return id;
	}

	public int getPower() {
		return power;
	}

	public int getTechLevelLimit() {
		return techLevelLimit;
	}

	public String getNeedItem() {
		return needItem;
	}

	public int getCoreRankLevel() {
		return coreRankLevel;
	}

	public String getCoreRankAttr() {
		return coreRankAttr;
	}

	public String getCoreRankSkill() {
		return coreRankEffectId;
	}
	
	public int getAtkAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(atkAttr).getOrDefault(soldierType, 0);
	}

    public int getHpAttr(int soldierType) {
        return SerializeHelper.cfgStr2Map(hpAttr).getOrDefault(soldierType, 0);
    }

	public static MechaCoreRankLevelCfg getCfgByLevel(int level) {
		return levelCfgMap.get(level);
	}

}
