package com.hawk.activity.type.impl.baseBuild.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;


/**
 * 基地飞升活动建筑列表
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "activity/base_build/%s/base_build.xml", autoLoad=false, loadParams="161")
public class BaseBuildCfg extends HawkConfigBase {
	/** */
	@Id
	private final int id;

	/** 建筑目标Id */
	private final int buildId;

	/** 建筑类型 */
	private final int buildType;

	/** 是否DIY建筑-资源/征兵/医院 */
	private final int isDIY;

	public BaseBuildCfg() {
		id = 0;
		buildId = 0;
		buildType = 0;
		isDIY = 0;
	}

	public int getId() {
		return id;
	}

	public int getBuildId() {
		return buildId;
	}

	public int getBuildType() {
		return buildType;
	}

	public boolean isDIY() {
		return isDIY == 1;
	}

}
