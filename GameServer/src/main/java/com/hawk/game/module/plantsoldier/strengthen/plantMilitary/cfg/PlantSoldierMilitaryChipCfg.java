package com.hawk.game.module.plantsoldier.strengthen.plantMilitary.cfg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.hawk.game.module.plantsoldier.strengthen.instrumentUpgrade.cfg.PlantInstrumentUpgradeChipCfg;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 泰能兵军衔
 * @author Golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/plant_soldier_military_chip.xml")
public class PlantSoldierMilitaryChipCfg extends HawkConfigBase {
	@Id
	protected final int id;

	/**
	 * 阶段(1-尉官阶段)
	 */
	protected final int stage;
	
	/**
	 * 兵种类型
	 */
	protected final int soldierType;
	
	/**
	 * 兵种部件
	 */
	protected final int group;
	
	/**
	 * 部件等级
	 */
	protected final int level;
	
	/**
	 * 部件战力
	 */
	protected final int power;

	/**
	 * 上一等级(阶段)
	 */
	private final int frontStage;

	/**
	 * 下一等级(阶段)
	 */
	private final int postStage;

	/**
	 * 升到此等级的消耗
	 */
	protected final String buildCost;

	/**
	 * 作用号
	 */
	protected final String effect;

	/**
	 * 技能
	 */
	protected final String skill;
	
	/**
	 * 攻击加成
	 */
	protected final String atkAttr;
	
	/**
	 * 生命加成
	 */
	protected final String hpAttr;
	
	protected final String beforeSkill;
	
	
	/**
	 * 基础攻击属性加成
	 */
	private final String baseAtkAttr;
	
	/**
	 * 基础血量属性加成
	 */
	private final String baseHpAttr;
	
	
	/**
	 * 作用号
	 */
	private Map<EffType, Integer> effectList;

	public PlantSoldierMilitaryChipCfg() {
		id = 0;
		stage = 0;
		soldierType = 0;
		group = 0;
		level = 0;
		effect = "";
		frontStage = 0;
		postStage = 0;
		buildCost = "";
		power = 0;
		skill = "";
		atkAttr = "";
		hpAttr = "";
		beforeSkill = "";
		baseAtkAttr = "";
		baseHpAttr = "";
	}

	@Override
	protected boolean assemble() {
		Map<EffType, Integer> map = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(effect)) {
			List<String> array = Splitter.on("|").omitEmptyStrings().splitToList(effect);
			for (String val : array) {
				String[] info = val.split("_");
				map.put(EffType.valueOf(Integer.parseInt(info[0])), Integer.parseInt(info[1]));
			}
		}
		effectList = ImmutableMap.copyOf(map);
		return true;
	}

	@Override
	protected boolean checkValid() {
		if (frontStage != 0) {
			PlantSoldierMilitaryChipCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PlantSoldierMilitaryChipCfg.class, frontStage);
			if (cfg == null) {
				HawkLog.errPrintln("PlantSoldierMilitaryCfg check valid failed, id: {}, frontStage: {}", id, frontStage);
				return false;
			}
		}
		if (postStage != 0) {
			PlantSoldierMilitaryChipCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PlantSoldierMilitaryChipCfg.class, postStage);
			if (cfg == null) {
				HawkLog.errPrintln("PlantSoldierMilitaryCfg check valid failed, id: {}, postStage: {}", id, postStage);
				return false;
			}
		}
		return true;
	}

	public int getId() {
		return id;
	}

	public int getLevel() {
		return level;
	}

	public int getFrontStage() {
		return frontStage;
	}

	public int getPostStage() {
		return postStage;
	}

	public String getBuildCost() {
		return buildCost;
	}

	public String getEffect() {
		return effect;
	}

	public Map<EffType, Integer> getEffectList() {
		return effectList;
	}

	public int getPower() {
		return power;
	}

	public int getAtkAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(atkAttr).getOrDefault(soldierType, 0);
	}

	public int getHpAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(hpAttr).getOrDefault(soldierType, 0);
	}

	public int getStage() {
		return stage;
	}

	public int getSoldierType() {
		return soldierType;
	}

	public int getGroup() {
		return group;
	}

	public String getSkill() {
		return skill;
	}

	public String getAtkAttr() {
		return atkAttr;
	}

	public String getHpAttr() {
		return hpAttr;
	}

	public String getBeforeSkill() {
		return beforeSkill;
	}

	public void setEffectList(Map<EffType, Integer> effectList) {
		this.effectList = effectList;
	}
	
	public int getBaseAtkAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(baseAtkAttr).getOrDefault(soldierType, 0);
	}

	public int getBaseHpAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(baseHpAttr).getOrDefault(soldierType, 0);
	}
}
