package com.hawk.activity.type.impl.commonExchangeTwo;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.commonExchangeTwo.cfg.CommonExchangeTwoActivityTimeCfg;
import com.hawk.activity.type.impl.commonExchangeTwo.cfg.CommonExchangeTwoKVConfig;

public class CommonExchangeTwoTimeController extends ExceptCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		CommonExchangeTwoKVConfig cfg = HawkConfigManager.getInstance().getKVInstance(CommonExchangeTwoKVConfig.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return CommonExchangeTwoActivityTimeCfg.class;
	}

}
