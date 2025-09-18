package com.hawk.activity.type.impl.gratitudeGift;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.JoinCurrentTermTimeController;
import com.hawk.activity.type.impl.gratitudeGift.cfg.GratitudeGiftActivityKVCfg;
import com.hawk.activity.type.impl.gratitudeGift.cfg.GratitudeGiftActivityTimeCfg;

public class GratitudeGiftTimeController extends JoinCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		GratitudeGiftActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(GratitudeGiftActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return GratitudeGiftActivityTimeCfg.class;
	}

}
