package com.hawk.activity.type.impl.heroTheme;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.heroTheme.cfg.HeroThemeActivityKVCfg;
import com.hawk.activity.type.impl.heroTheme.cfg.HeroThemeActivityTimeCfg;

public class HeroThemeTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return HeroThemeActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		HeroThemeActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HeroThemeActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}
}
