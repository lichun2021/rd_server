package com.hawk.activity.type.impl.domeExchangeTwo;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.domeExchangeTwo.cfg.DomeActivityTwoKVConfig;
import com.hawk.activity.type.impl.domeExchangeTwo.cfg.DomeActivityTwoTimeCfg;

public class DomeExchangeTwoTimeController extends ExceptCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		DomeActivityTwoKVConfig cfg = HawkConfigManager.getInstance().getKVInstance(DomeActivityTwoKVConfig.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return DomeActivityTwoTimeCfg.class;
	}

}
