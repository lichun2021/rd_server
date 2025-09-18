package com.hawk.activity.type.impl.urlModelSix;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.urlModelSix.cfg.UrlModelSixActivityKVCfg;
import com.hawk.activity.type.impl.urlModelSix.cfg.UrlModelSixActivityTimeCfg;

public class UrlModelSixTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return UrlModelSixActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		UrlModelSixActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(UrlModelSixActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
