package com.hawk.activity.type.impl.urlModel344;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ServerOpenTimeController;
import com.hawk.activity.type.impl.urlModel344.cfg.UrlModel344KVCfg;
import com.hawk.activity.type.impl.urlModel344.cfg.UrlModel344TimeCfg;

public class UrlModel344TimeController extends ServerOpenTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return UrlModel344TimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		UrlModel344KVCfg cfg = HawkConfigManager.getInstance().getKVInstance(UrlModel344KVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
