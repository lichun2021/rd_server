package com.hawk.activity.type.impl.sendFlower.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "activity/send_flower/send_flower_count.xml")
public class SendFlowerCountCfg extends HawkConfigBase {
	@Id
	private final int id;// ="1"
	private final String count;// ="30000_1150030_1"
	private final String money;// ="10000_1001_10"

	public SendFlowerCountCfg() {
		id = 0;
		count = "";
		money = "";
	}

	@Override
	protected boolean assemble() {
		return true;
	}

	public int getId() {
		return id;
	}

	public String getCount() {
		return count;
	}

	public String getMoney() {
		return money;
	}

}