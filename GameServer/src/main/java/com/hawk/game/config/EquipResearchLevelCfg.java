package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.cfgElement.EffectObject;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 装备研究等级表
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/equip_research_node_level.xml")
public class EquipResearchLevelCfg extends HawkConfigBase {

	@Id
	protected final int id;

	/**
	 * 等级
	 */
	protected final int level;

	/**
	 * 研究id
	 */
	protected final int researchId;

	/**
	 * 属性
	 */
	protected final String attr;

	/**
	 * 固定属性加成
	 */
	protected final int fixAttr;
	
	/**
	 * 随机属性加成
	 */
	protected final int randAttr;

	/**
	 * 套装属性加成
	 */
	protected final int suitAttr;

	/**
	 * 升级消耗
	 */
	protected final String cost;

	/**
	 * 战力
	 */
	protected final int power;

	/**
	 * 升级所需时间
	 */
	protected final int time;

	protected final String atkAttr;
	protected final String hpAttr;
	
	/**
	 * 属性作用号
	 */
	private List<EffectObject> attrEff;
	
	/**
	 * 构造
	 */
	public EquipResearchLevelCfg() {
		id = 0;
		level = 0;
		researchId = 0;
		attr = "";
		fixAttr = 0;
		randAttr = 0;
		suitAttr = 0;
		cost = "";
		power = 0;
		time = 0;
		atkAttr = "";
		hpAttr = "";
	}

	public int getId() {
		return id;
	}

	public int getLevel() {
		return level;
	}

	public int getResearchId() {
		return researchId;
	}

	public String getAttr() {
		return attr;
	}

	public int getFixAttr() {
		return fixAttr;
	}

	public int getRandAttr() {
		return randAttr;
	}

	public int getSuitAttr() {
		return suitAttr;
	}

	public String getCost() {
		return cost;
	}

	public int getPower() {
		return power;
	}

	public int getTime() {
		return time;
	}
	
	public int getAtkAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(atkAttr).getOrDefault(soldierType, 0);
	}

	public int getHpAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(hpAttr).getOrDefault(soldierType, 0);
	}
	
	@Override
	protected boolean assemble() {
		List<EffectObject> attrEff = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(attr)) {
			attrEff = SerializeHelper.stringToList(EffectObject.class, attr, SerializeHelper.BETWEEN_ITEMS, SerializeHelper.ATTRIBUTE_SPLIT, new ArrayList<>());
		}
		this.attrEff = attrEff;
		return true;
	}

	public List<EffectObject> getAttrEff() {
		return attrEff;
	}
	
}
