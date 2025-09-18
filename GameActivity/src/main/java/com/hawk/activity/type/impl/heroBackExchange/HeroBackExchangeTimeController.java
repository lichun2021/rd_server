package com.hawk.activity.type.impl.heroBackExchange;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.heroBackExchange.cfg.HeroBackExchangeKVConfig;
import com.hawk.activity.type.impl.heroBackExchange.cfg.HeroBackExchangeTimeCfg;

public class HeroBackExchangeTimeController extends ExceptCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		HeroBackExchangeKVConfig cfg = HawkConfigManager.getInstance().getKVInstance(HeroBackExchangeKVConfig.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return HeroBackExchangeTimeCfg.class;
	}

}
