package com.hawk.activity.type.impl.gratitudeGift.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "activity/gratitude_gift/gratitude_gift.xml")
public class GratitudeGiftActivityCfg extends HawkConfigBase {

	@Id
	private final int id;
	private final String rewards;

	public GratitudeGiftActivityCfg() {
		id = 0;
		rewards = "";
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

}
