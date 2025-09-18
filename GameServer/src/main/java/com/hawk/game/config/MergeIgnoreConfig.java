package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "cfg/merge/mergeIgnore.xml")
public class MergeIgnoreConfig extends HawkConfigBase {
	@Id
	protected final String activity;
	
	protected final int id;
	
	protected final String desc;
	
	public MergeIgnoreConfig() {
		this.activity = "";
		this.id = 0;
		this.desc = "";
	}

	public int getId() {
		return id;
	}

	public String getActivity() {
		return activity;
	}

	public String getDesc() {
		return desc;
	}
}