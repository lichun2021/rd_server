package com.hawk.game.module.plantsoldier.strengthen.crystalAnalysis.cfg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.protocol.Const.EffType;

@HawkConfigManager.XmlResource(file = "xml/plant_crystal_analysis.xml")
public class PlantCrystalAnalysisCfg extends HawkConfigBase {

	@Id
	protected final int id;
	// 类型
	protected final int level;
	protected final int power;// ="999"
	protected final int maxChipLevel;
	protected final String chips;
	// 前置条件,必须创建前面的建筑
	protected final String frontBuild;

	/** 上一等级(阶段)建筑*/
	private final int frontStage;

	/** 下一等级(阶段)建筑*/
	private final int postStage;

	// 资源消耗 type_id_count,type_id_count
	protected final String buildCost;

	protected final String effect;

	// 前置建筑
	protected int[] chipIds = new int[0];
	protected int[] frontBuildIds = new int[0];
	private Map<EffType, Integer> effectList;

	private static PlantCrystalAnalysisCfg zeroLevelCfg;

	public PlantCrystalAnalysisCfg() {
		id = 0;
		level = 0;
		power = 0;
		chips = "";
		maxChipLevel = 0;
		frontBuild = "";
		frontStage = 0;
		postStage = 0;
		buildCost = "";
		effect = "";
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

		if (chips != null && !"".equals(chips) && !"0".equals(chips)) {
			String[] ids = chips.split(",");
			chipIds = new int[ids.length];
			int index = 0;
			for (String frontId : ids) {
				chipIds[index] = Integer.parseInt(frontId);
				index++;
			}
		}

		if (level == 0 && frontStage == 0) {
			zeroLevelCfg = this;
		}

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
			PlantCrystalAnalysisCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PlantCrystalAnalysisCfg.class, frontStage);
			if (cfg == null) {
				HawkLog.errPrintln("PlantTechnologyCfg check valid failed, id: {}, frontStage: {}", id, frontStage);
				return false;
			}
		}
		if (postStage != 0) {
			PlantCrystalAnalysisCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PlantCrystalAnalysisCfg.class, postStage);
			if (cfg == null) {
				HawkLog.errPrintln("PlantTechnologyCfg check valid failed, id: {}, postStage: {}", id, postStage);
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

	public int getMaxChipLevel() {
		return maxChipLevel;
	}

	public String getChips() {
		return chips;
	}

	public String getEffect() {
		return effect;
	}

	public int[] getChipIds() {
		return chipIds;
	}

	public Map<EffType, Integer> getEffectList() {
		return effectList;
	}

	public int getPower() {
		return power;
	}

	public static PlantCrystalAnalysisCfg getZeroLevelCfg() {
		return zeroLevelCfg;
	}

}
