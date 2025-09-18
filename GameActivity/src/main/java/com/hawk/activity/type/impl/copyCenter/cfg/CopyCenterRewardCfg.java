package com.hawk.activity.type.impl.copyCenter.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRandObj;

@HawkConfigManager.XmlResource(file = "activity/copy_center/copy_center_reward.xml")
public class CopyCenterRewardCfg extends HawkConfigBase implements HawkRandObj {

	@Id
	private final int id;
	private final String rewards;
	private final int weight;
	private final int type;

	public CopyCenterRewardCfg() {
		id = 0;
		rewards = "";
		weight = 0;
		type = 1;
	}

	@Override
	protected boolean assemble() {
		return true;
	}

	@Override
	protected final boolean checkValid() {
		return super.checkValid();
	}

	public int getId() {
		return id;
	}

	public String getRewards() {
		return rewards;
	}

	@Override
	public int getWeight() {
		return weight;
	}

	public int getType() {
		return type;
	}

}
