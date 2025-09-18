package com.hawk.game.module.plantsoldier.strengthen.soldierStrengthen.cfg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.helper.HawkAssert;
import org.hawk.os.HawkOSOperator;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.XmlResource(file = "xml/plant_soldier_strengthen_tech.xml")
public class PlantSoldierStrengthenTechCfg extends HawkConfigBase {
	@Id
	protected final int id;
	/**
	 * 科技等级
	 */
	protected final int techLevel;
	/**
	 * 科技类型
	 */
	protected final int techId;
	protected final int pointId;
	protected final int group;
	/**
	 * 科技研究科研石消耗
	 */
	protected final String techItem;
	/**
	 * 科技研究资源消耗
	 */
	protected final String techCost;
	/**
	 * 解锁条件-前置科技
	 */
	protected final String frontTech;
	/**
	 * 解锁条件-前置建筑
	 */
	protected final String frontBuild;
	/**
	 * 科技作用号
	 */
	protected final String techEffect;
	/**
	 * 科技技能
	 */
	protected final int techSkill;
	/**
	 * 科技战力
	 */
	protected final int battlePoint;
	/**
	 * 科技类型 
	 */
	protected final int soldierType;
	/**
	 * 基础攻击战力加成
	 */
	protected final String baseAtkAttr;
	/**
	 * 基础血量战力加成
	 */
	protected final String baseHpAttr;

	/**
	 * 科技作用号属性
	 */
	private Map<EffType, Integer> effectList;
	/**
	 * 前置解锁科技列表
	 */
	private List<Integer> conditionTechList;
	/**
	 * 前置解锁建筑列表
	 */
	protected int[] frontBuildIds = new int[0];

	private static Map<Integer, Integer> techIdLevelMaxMap = new HashMap<Integer, Integer>();
	private SoldierType type;

	public PlantSoldierStrengthenTechCfg() {
		this.id = 0;
		this.techLevel = 0;
		this.techId = 0;
		this.group = 0;
		this.pointId = 0;
		this.techEffect = "";
		this.techItem = "";
		this.techCost = "";
		this.frontTech = "";
		this.frontBuild = "";
		this.battlePoint = 0;
		this.techSkill = 0;
		this.soldierType = 0;
		this.baseAtkAttr = "";
		this.baseHpAttr = "";
	}

	/**
	 * @return 科技Id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return 科技等级
	 */
	public int getLevel() {
		return techLevel;
	}

	/**
	 * @return 科技类型Id
	 */
	public int getTechId() {
		return techId;
	}

	/**
	 * @return 解锁技能
	 */
	public int getTechSkill() {
		return techSkill;
	}

	public Map<EffType, Integer> getEffectList() {
		return effectList;
	}

	/**
	 * @return 升级所需科技列表
	 */
	public List<Integer> getConditionTechList() {
		return Collections.unmodifiableList(conditionTechList);
	}

	public int[] getFrontBuildIds() {
		return frontBuildIds;
	}

	/**
	 * @return 科技包含的战斗力
	 */
	public int getBattlePoint() {
		return battlePoint;
	}

	public int getSoldierType() {
		return soldierType;
	}

	@Override
	protected boolean checkValid() {
		
		boolean isId = (id - 400000) / 100 == techId;
		boolean isLevel = id % 100 == techLevel;
		HawkAssert.isTrue(isId);
		HawkAssert.isTrue(isLevel);
		
		for (Integer techId : conditionTechList) {
			PlantSoldierStrengthenTechCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PlantSoldierStrengthenTechCfg.class, techId);
			if (cfg == null) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected boolean assemble() {

		Map<EffType, Integer> map = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(techEffect)) {
			List<String> array = Splitter.on("|").omitEmptyStrings().splitToList(techEffect);
			for (String val : array) {
				String[] info = val.split("_");
				map.put(EffType.valueOf(Integer.parseInt(info[0])), Integer.parseInt(info[1]));
			}
		}
		effectList = ImmutableMap.copyOf(map);

		conditionTechList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(frontTech)) {
			String[] array = frontTech.split(";");
			for (String val : array) {
				conditionTechList.add(Integer.parseInt(val));
			}
		}

		// 前置建筑id
		if (frontBuild != null && !"".equals(frontBuild) && !"0".equals(frontBuild)) {
			String[] ids = frontBuild.split(",");
			frontBuildIds = new int[ids.length];
			int index = 0;
			for (String frontId : ids) {
				frontBuildIds[index] = Integer.parseInt(frontId);
				index++;
			}
		}

		if (!techIdLevelMaxMap.containsKey(techId) || techLevel > techIdLevelMaxMap.get(techId)) {
			techIdLevelMaxMap.put(techId, techLevel);
		}
		type = SoldierType.valueOf(soldierType);
		return true;
	}

	public static Map<Integer, Integer> getTechIdLevelMaxMap() {
		return techIdLevelMaxMap;
	}

	public int getTechLevel() {
		return techLevel;
	}

	public String getTechItem() {
		return techItem;
	}

	public String getTechCost() {
		return techCost;
	}

	public String getFrontTech() {
		return frontTech;
	}

	public String getFrontBuild() {
		return frontBuild;
	}

	public String getTechEffect() {
		return techEffect;
	}

	public SoldierType getType() {
		return type;
	}

	public int getBaseAtkAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(baseAtkAttr).getOrDefault(soldierType, 0);
	}

	public int getBaseHpAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(baseHpAttr).getOrDefault(soldierType, 0);
	}

	public int getGroup() {
		return group;
	}

	public int getPointId() {
		return pointId;
	}
	
}
