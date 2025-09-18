package com.hawk.activity.type.impl.brokenexchangeThree;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.brokenexchangeThree.cfg.ActivityExchangeThreeKVCfg;
import com.hawk.activity.type.impl.brokenexchangeThree.cfg.ActivityExchangeThreeTimeCfg;

public class BrokenExchangeThreeTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return ActivityExchangeThreeTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		ActivityExchangeThreeKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(ActivityExchangeThreeKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
