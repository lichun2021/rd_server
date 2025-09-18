package com.hawk.activity.type.impl.urlModelFive;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.urlModelFive.cfg.UrlModelFiveActivityKVCfg;
import com.hawk.activity.type.impl.urlModelFive.cfg.UrlModelFiveActivityTimeCfg;

public class UrlModelFiveTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return UrlModelFiveActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		UrlModelFiveActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(UrlModelFiveActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
