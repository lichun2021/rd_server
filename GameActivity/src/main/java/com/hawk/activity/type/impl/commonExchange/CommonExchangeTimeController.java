package com.hawk.activity.type.impl.commonExchange;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.commonExchange.cfg.ComeBackExchangeActivityTimeCfg;
import com.hawk.activity.type.impl.commonExchange.cfg.CommonExchangeKVConfig;

public class CommonExchangeTimeController extends ExceptCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		CommonExchangeKVConfig cfg = HawkConfigManager.getInstance().getKVInstance(CommonExchangeKVConfig.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return ComeBackExchangeActivityTimeCfg.class;
	}

}
