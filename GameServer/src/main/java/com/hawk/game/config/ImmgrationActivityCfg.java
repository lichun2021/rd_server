package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 迁服活动表(哪些活动数据迁移)
 * @author Golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/immgration_activity.xml")
public class ImmgrationActivityCfg extends HawkConfigBase {
	
	protected final int activityId;

	public ImmgrationActivityCfg() {
		this.activityId = 0;
	}

	public int getActivityId() {
		return activityId;
	}
}
