package com.hawk.robot.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 装备品质配置
 *
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/equipment_quality.xml")
public class EquipQualityCfg extends HawkConfigBase {
	@Id
	/** 装备品质 */
	protected final int qualityId;

	/** 升品所需装备数量 */
	protected final int qualityUpEquipNum;

	/** 升阶所需材料id */
	protected final int gradeUpMaterialId;

	public EquipQualityCfg() {
		qualityId = 0;
		qualityUpEquipNum = 0;
		gradeUpMaterialId = 0;
	}

	public int getQualityId() {
		return qualityId;
	}

	public int getQualityUpEquipNum() {
		return qualityUpEquipNum;
	}

	public int getGradeUpMaterialId() {
		return gradeUpMaterialId;
	}
}
