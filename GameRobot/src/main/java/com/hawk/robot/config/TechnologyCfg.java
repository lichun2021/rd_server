package com.hawk.robot.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

/**
 * 科技功能配置
 * 
 * @author shadow
 *
 */
@HawkConfigManager.XmlResource(file = "xml/tech_level.xml")
public class TechnologyCfg extends HawkConfigBase {
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
	/**
	 * 科技研究耗时
	 */
	protected final long techTime;
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
	 * 解锁条件-VIP等级
	 */
	protected final int frontVip;
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
	protected final int techType;
	
	/**
	 * 前置解锁科技列表
	 */
	private List<Integer> conditionTechList;
	/**
	 * 前置解锁建筑列表
	 */
	private List<Integer> conditionBuildList;
	
	private static Map<Integer, List<Integer>> preBuildTechMap = new HashMap<>();
	
	private static Map<Integer, List<Integer>> preTechMap = new HashMap<>();
	
	private static Map<Integer, Integer> techIdMaxLevel = new HashMap<>(); 
	
	public TechnologyCfg() {
		this.id = 0;
		this.techLevel = 0;
		this.techId = 0;
		this.techEffect = "";
		this.techTime = 0;
		this.techItem = "";
		this.techCost = "";
		this.frontTech = "";
		this.frontBuild = "";
		this.frontVip = 0;
		this.battlePoint = 0;
		this.techSkill = 0;
		this.techType = 0;
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
	 * @return 升级所需时间
	 */
	public long getLevelUpTime() {
		return techTime;
	}
	
	/**
	 * @return 解锁技能
	 */
	public int getTechSkill() {
		return techSkill;
	}

	/**
	 * @return 升级所需VIP等级
	 */
	public int getFrontVip() {
		return frontVip;
	}
	
		/**
	 * 科技类型
	 * @return
	 */
	public int getTechType() {
		return techType;
	}
	
	/**
	 * @return 科技包含的战斗力
	 */
	public int getBattlePoint() {
		return battlePoint;
	}
	
	/**
	 * @return 升级所需科技列表
	 */
	public List<Integer> getConditionTechList() {
		return conditionTechList;
	}

	/**
	 * @return 升级所需科技建筑等级
	 */
	public List<Integer> getConditionBuildList() {
		return conditionBuildList;
	}

	@Override
	protected boolean checkValid() {
		for (Integer techId : conditionTechList) {
			TechnologyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(TechnologyCfg.class, techId);
			if (cfg == null) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected boolean assemble() {
		conditionTechList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(frontTech)) {
			String[] array = frontTech.split(";");
			for (String val : array) {
				int techId = Integer.parseInt(val);
				conditionTechList.add(techId);
				List<Integer> unlockTechList = preTechMap.get(techId);
				if(unlockTechList == null) {
					unlockTechList = new ArrayList<>();
					preTechMap.put(techId, unlockTechList);
				}
				unlockTechList.add(id);
			}
		}

		conditionBuildList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(frontBuild)) {
			String[] array = frontBuild.split("_");
			for (String val : array) {
				int buildId = Integer.parseInt(val);
				conditionBuildList.add(buildId);
				List<Integer> unlockTechList = preBuildTechMap.get(buildId);
				if(unlockTechList == null) {
					unlockTechList = new ArrayList<>();
					preBuildTechMap.put(buildId, unlockTechList);
				}
				unlockTechList.add(id);
			}
		}
		
		Integer levelInteger = techIdMaxLevel.get(techId);
		if (levelInteger == null || techLevel > levelInteger) {
			techIdMaxLevel.put(techId, techLevel);
		}
		
		return true;
	}
	
	public static List<Integer> getUnlockTechByBuildId(int buildId) {
		return preBuildTechMap.get(buildId);
	}
	
	public static List<Integer> getUnlockTechByTechId(int techId) {
		return preTechMap.get(techId);
	}
	
	public static int getMaxLevelByTechId(int techId) {
		if (techIdMaxLevel.containsKey(techId)) {
			return techIdMaxLevel.get(techId);
		}
		
		return 0;
	}
}
