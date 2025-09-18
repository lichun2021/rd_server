package com.hawk.activity.type.impl.virtualLaboratory.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;


/**
 * 虚拟实验室活动配置
 */
@HawkConfigManager.XmlResource(file = "activity/armed_science/armed_science_package.xml")
public class VirtualLaboratoryCardCfg extends HawkConfigBase {
	/** 成就id*/
	@Id
	private final int id;

	public VirtualLaboratoryCardCfg() {
		id = 0;
	}

	public int getId() {
		return id;
	}
	
}
