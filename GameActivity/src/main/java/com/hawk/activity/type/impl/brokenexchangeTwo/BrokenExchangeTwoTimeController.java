package com.hawk.activity.type.impl.brokenexchangeTwo;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ServerOpenTimeController;
import com.hawk.activity.type.impl.brokenexchangeTwo.cfg.ActivityExchangeTwoKVCfg;
import com.hawk.activity.type.impl.brokenexchangeTwo.cfg.ActivityExchangeTwoTimeCfg;

public class BrokenExchangeTwoTimeController extends ServerOpenTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return ActivityExchangeTwoTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		ActivityExchangeTwoKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(ActivityExchangeTwoKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
