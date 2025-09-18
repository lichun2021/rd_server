package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

@HawkConfigManager.XmlResource(file = "cfg/separate/separateActivity.xml")
public class SeparateActivityCfg extends HawkConfigBase {
	@Id
	protected final String activity;
	
	protected final int id;
	
	protected final String desc;
	
	static List<Integer> activityIdList = new ArrayList<>();
	
	public SeparateActivityCfg() {
		this.activity = "";
		this.id = 0;
		this.desc = "";
	}
	
	@Override
	public boolean assemble() {
		if (!HawkOSOperator.isEmptyString(desc)) {
			int activityId = Integer.parseInt(desc);
			if (activityIdList.contains(activityId)) {
				return false;
			}
			activityIdList.add(activityId);
		}
		return true;
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
