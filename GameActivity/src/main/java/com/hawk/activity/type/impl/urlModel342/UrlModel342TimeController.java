package com.hawk.activity.type.impl.urlModel342;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.urlModel342.cfg.UrlModel342KVCfg;
import com.hawk.activity.type.impl.urlModel342.cfg.UrlModel342TimeCfg;

public class UrlModel342TimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return UrlModel342TimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		UrlModel342KVCfg cfg = HawkConfigManager.getInstance().getKVInstance(UrlModel342KVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
