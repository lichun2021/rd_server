package com.hawk.activity.type.impl.preferential_surprise;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.preferential_surprise.cfg.PreferentialSupriseKVCfg;
import com.hawk.activity.type.impl.preferential_surprise.cfg.PreferentialSupriseTimeCfg;

public class PreferentialSupriseTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return PreferentialSupriseTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		PreferentialSupriseKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PreferentialSupriseKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
