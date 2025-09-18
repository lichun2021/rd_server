package com.hawk.activity.type.impl.heroBack;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.heroBack.cfg.HeroBackActivityTimeCfg;
import com.hawk.activity.type.impl.heroBack.cfg.HeroBackKVConfig;

public class HeroBackTimeController extends ExceptCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		HeroBackKVConfig cfg = HawkConfigManager.getInstance().getKVInstance(HeroBackKVConfig.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return HeroBackActivityTimeCfg.class;
	}
}
