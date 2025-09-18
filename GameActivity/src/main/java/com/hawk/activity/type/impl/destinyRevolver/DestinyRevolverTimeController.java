package com.hawk.activity.type.impl.destinyRevolver;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.destinyRevolver.cfg.DestinyRevolverCfg;
import com.hawk.activity.type.impl.destinyRevolver.cfg.DestinyRevolverTimeCfg;

public class DestinyRevolverTimeController extends ExceptCurrentTermTimeController {
	@Override
	public long getServerDelay() {
		DestinyRevolverCfg cfg = HawkConfigManager.getInstance().getKVInstance(DestinyRevolverCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return DestinyRevolverTimeCfg.class;
	}

}
