package com.hawk.activity.type.impl.urlModel343;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.urlModel343.cfg.UrlModel343KVCfg;
import com.hawk.activity.type.impl.urlModel343.cfg.UrlModel343TimeCfg;

public class UrlModel343TimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return UrlModel343TimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		UrlModel343KVCfg cfg = HawkConfigManager.getInstance().getKVInstance(UrlModel343KVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
