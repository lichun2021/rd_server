package com.hawk.game.module.plantsoldier.strengthen.plantMilitary.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 泰能兵军衔
 * @author Golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/plant_soldier_military.xml")
public class PlantSoldierMilitaryCfg extends HawkConfigBase {
	
	/**
	 * 兵种
	 */
	@Id
	protected final int soldierType;

	/**
	 * 初始化部件(兵种)
	 */
	protected final String initChips;
	
	/**
	 * 前置强化等级
	 */
	protected final int frontStrengthen;
	
	/**
	 * 前置建筑等级
	 */
	protected final String frontBuild;

	protected int[] frontBuildIds = new int[0];
	
	public PlantSoldierMilitaryCfg() {
		soldierType = 0;
		initChips = "";
		frontStrengthen = 0;
		frontBuild = "";
	}

	public int getSoldierType() {
		return soldierType;
	}

	public String[] getInitChips() {
		return initChips.split(",");
	}

	public int getFrontStrengthen() {
		return frontStrengthen;
	}

	public String getFrontBuild() {
		return frontBuild;
	}

	public int[] getFrontBuildIds() {
		return frontBuildIds;
	}

	@Override
	protected boolean assemble() {
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
		return true;
	}
}
