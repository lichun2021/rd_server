package com.hawk.game.module.plantsoldier.strengthen.soldierStrengthen.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;

import com.hawk.game.config.BuildingCfg;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * 建筑配置
 *
 * @author julia
 *
 */
@HawkConfigManager.XmlResource(file = "xml/plant_soldier_strengthen.xml")
public class PlantSoldierStrengthenCfg extends HawkConfigBase {

	@Id
	protected final int id;
	// SoldierType
	protected final int soldierType;
	// 科技节点id. 每满足一个就强一阶
	protected final String stepTech;
	// 前置条件,必须创建前面的建筑
	protected final String frontBuild;

	// 前置建筑
	protected int[] stepTechIds = new int[0];
	protected int[] frontBuildIds = new int[0];

	private SoldierType type;

	public PlantSoldierStrengthenCfg() {
		id = 0;
		frontBuild = "";
		stepTech = "";
		soldierType = 0;
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

		if (stepTech != null && !"".equals(stepTech) && !"0".equals(stepTech)) {
			String[] ids = stepTech.split(";");
			stepTechIds = new int[ids.length];
			int index = 0;
			for (String frontId : ids) {
				stepTechIds[index] = Integer.parseInt(frontId);
				index++;
			}
		}

		type = SoldierType.valueOf(soldierType);
		return true;
	}

	@Override
	protected boolean checkValid() {
		// 前置建筑id
		if (frontBuildIds != null) {
			for (int frontId : frontBuildIds) {
				BuildingCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, frontId);
				if (cfg == null) {
					HawkLog.errPrintln("plantFactoryCfg check valid failed, front buildCfgId: {}, buildingCfg: {}", frontId, cfg);
					return false;
				}
			}
		}

		return true;
	}

	public int getId() {
		return id;
	}

	public int[] getStepTechIds() {
		return stepTechIds;
	}

	public int[] getFrontBuildIds() {
		return frontBuildIds;
	}

	public String getFrontBuild() {
		return frontBuild;
	}

	public SoldierType getType() {
		return type;
	}

}
