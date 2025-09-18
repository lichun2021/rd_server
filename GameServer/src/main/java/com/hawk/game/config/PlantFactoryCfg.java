package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;

import com.hawk.game.util.GsConst;

/**
 * 建筑配置
 *
 * @author julia
 *
 */
@HawkConfigManager.XmlResource(file = "xml/plant_factory.xml")
public class PlantFactoryCfg extends HawkConfigBase {
	@Id
	protected final int id;
	// 类型
	protected final int level;
	// 生产线类型
	protected final int factoryType;

	// 前置条件,必须创建前面的建筑
	protected final String frontBuild;
	// 前置生产线
	protected final String frontPlant;

	/** 上一等级(阶段)建筑*/
	private final int frontStage;

	/** 下一等级(阶段)建筑*/
	private final int postStage;

	// 资源消耗 type_id_count,type_id_count
	protected final String buildCost;

	// 产资源每小时
	protected final double itemPerDay;// ="0.333"
	protected final int itemId;// ="850000"
	protected final int itemLimit;// ="1"

	// 前置建筑
	protected int[] frontBuildIds = new int[0];
	protected int[] frontPlantIds = new int[0];

	private int collectOneUseMil;

	public PlantFactoryCfg() {
		id = 0;
		level = 0;
		factoryType = 0;
		frontBuild = "";
		frontPlant = "";
		frontStage = 0;
		postStage = 0;
		buildCost = "";
		itemPerDay = 0;
		itemId = 0;
		itemLimit = 0;
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

		if (frontPlant != null && !"".equals(frontPlant) && !"0".equals(frontPlant)) {
			String[] ids = frontPlant.split(",");
			frontPlantIds = new int[ids.length];
			int index = 0;
			for (String frontId : ids) {
				frontPlantIds[index] = Integer.parseInt(frontId);
				index++;
			}
		}

		collectOneUseMil = (int) (GsConst.HOUR_MILLI_SECONDS * 24 / itemPerDay);

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
		if (frontStage != 0) {
			PlantFactoryCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PlantFactoryCfg.class, frontStage);
			if (cfg == null) {
				HawkLog.errPrintln("plantFactoryCfg check valid failed, id: {}, frontStage: {}", id, frontStage);
				return false;
			}
		}
		if (postStage != 0) {
			PlantFactoryCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PlantFactoryCfg.class, postStage);
			if (cfg == null) {
				HawkLog.errPrintln("plantFactoryCfg check valid failed, id: {}, postStage: {}", id, postStage);
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

	public int[] getFrontBuildIds() {
		return frontBuildIds;
	}

	public String getFrontPlant() {
		return frontPlant;
	}

	public int[] getFrontPlantIds() {
		return frontPlantIds;
	}

	public int getFactoryType() {
		return factoryType;
	}

	public String getFrontBuild() {
		return frontBuild;
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

	public double getItemPerDay() {
		return itemPerDay;
	}

	public int getItemId() {
		return itemId;
	}

	public int getItemLimit() {
		return itemLimit;
	}

	public int getCollectOneUseMil() {
		return collectOneUseMil;
	}

}
