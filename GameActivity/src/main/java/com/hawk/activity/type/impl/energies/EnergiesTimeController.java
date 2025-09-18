package com.hawk.activity.type.impl.energies;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.energies.cfg.EnergiesActivityKVCfg;
import com.hawk.activity.type.impl.energies.cfg.EnergiesActivityTimeCfg;

public class EnergiesTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return EnergiesActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		EnergiesActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(EnergiesActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}
}
