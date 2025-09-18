package com.hawk.activity.type.impl.urlModelFour;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.urlModelFour.cfg.UrlModelFourActivityKVCfg;
import com.hawk.activity.type.impl.urlModelFour.cfg.UrlModelFourActivityTimeCfg;

public class UrlModelFourTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return UrlModelFourActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		UrlModelFourActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(UrlModelFourActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
