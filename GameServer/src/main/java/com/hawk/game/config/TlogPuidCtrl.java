package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "cfg/puidCtrl/tlogPuidCtrl.xml")
public class TlogPuidCtrl extends HawkConfigBase {
	@Id
	protected final String puid;
	
	protected final String name;
	
	protected final int priority;
	
	public TlogPuidCtrl() {
		this.puid = "";
		this.name = "";
		this.priority = 0;
	}

	public String getPuid() {
		return puid;
	}

	public String getName() {
		return name;
	}

	public int getPriority() {
		return priority;
	}
}
