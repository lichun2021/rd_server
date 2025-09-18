package com.hawk.robot.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 装备阶级配置
 *
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/equipment_grade.xml")
public class EquipGradeCfg extends HawkConfigBase {
	@Id
	/** 装备阶级 */
	protected final int gradeId;

	/** 升阶所需道具数量 */
	protected final int gradeUpMaterialNum;

	public EquipGradeCfg() {
		gradeId = 0;
		gradeUpMaterialNum = 0;
	}

	public int getGradeId() {
		return gradeId;
	}

	public int getGradeUpMaterialNum() {
		return gradeUpMaterialNum;
	}
}
