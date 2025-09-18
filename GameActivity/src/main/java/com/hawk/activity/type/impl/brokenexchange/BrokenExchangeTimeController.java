package com.hawk.activity.type.impl.brokenexchange;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.brokenexchange.cfg.ActivityExchangeKVCfg;
import com.hawk.activity.type.impl.brokenexchange.cfg.ActivityExchangeTimeCfg;

public class BrokenExchangeTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return ActivityExchangeTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		ActivityExchangeKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(ActivityExchangeKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
