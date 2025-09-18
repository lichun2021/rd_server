package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "xml/activity_week_calendar.xml")
public class ActivityWeekCalenderCfg  extends HawkConfigBase {

	/**
	 * 活动ID
	 */
	@Id
	protected final int id;
	
	protected final int type;
	

	
	public ActivityWeekCalenderCfg() {
		super();
		this.id = 0;
		this.type = 0;
	}


	


	public int getId() {
		return id;
	}





	public int getType() {
		return type;
	}

	
	
	
	
	
	
}
