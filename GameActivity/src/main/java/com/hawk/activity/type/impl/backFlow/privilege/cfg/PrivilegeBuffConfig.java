package com.hawk.activity.type.impl.backFlow.privilege.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "activity/back_privilege_activity/back_privilege_activity_effect.xml")
public class PrivilegeBuffConfig extends HawkConfigBase{
	@Id
	private final int id;
	
	private final int buff;
	
	private final int bufftime;
	
	
	public PrivilegeBuffConfig(){
		id = 0;
		buff = 0;
		bufftime = 0;
	}

	
	
	public int getId() {
		return id;
	}


	public int getBuff() {
		return buff;
	}



	public int getBufftime() {
		return bufftime;
	}



	

	
	
}
