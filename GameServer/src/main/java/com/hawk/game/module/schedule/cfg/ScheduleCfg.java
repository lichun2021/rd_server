package com.hawk.game.module.schedule.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 待办事项功能配置
 */
@HawkConfigManager.XmlResource(file = "xml/schedule.xml")
public class ScheduleCfg extends HawkConfigBase {

	@Id
	protected final int id;
	
	protected final String title;
	
	protected final int alertTime;

	protected final int lifeTime;
	
	public ScheduleCfg() {
		id = 0;
		title = "";
		alertTime = 0;
		lifeTime = 0;
	}
	
	@Override
	protected boolean assemble() {
		return true;
	}

	public int getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public int getAlertTime() {
		return alertTime;
	}
	
	public int getLifeTime() {
		return lifeTime;
	}

}
