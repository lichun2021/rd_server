package com.hawk.activity.type.impl.domeExchange;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.domeExchange.cfg.DomeActivityKVConfig;
import com.hawk.activity.type.impl.domeExchange.cfg.DomeActivityTimeCfg;

public class DomeExchangeTimeController extends ExceptCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		DomeActivityKVConfig cfg = HawkConfigManager.getInstance().getKVInstance(DomeActivityKVConfig.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return DomeActivityTimeCfg.class;
	}

}
