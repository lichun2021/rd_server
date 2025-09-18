package com.hawk.activity.type.impl.roulette;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.roulette.cfg.RouletteKVCfg;
import com.hawk.activity.type.impl.roulette.cfg.RouletteTimeCfg;

public class RouletteTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return RouletteTimeCfg.class;
	}
	
	@Override
	public long getServerDelay() {
		RouletteKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(RouletteKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
