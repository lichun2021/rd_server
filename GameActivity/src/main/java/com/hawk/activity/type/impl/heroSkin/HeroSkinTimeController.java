package com.hawk.activity.type.impl.heroSkin;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.heroSkin.cfg.HeroSkinActivityKVCfg;
import com.hawk.activity.type.impl.heroSkin.cfg.HeroSkinActivityTimeCfg;

public class HeroSkinTimeController extends ExceptCurrentTermTimeController {
	
	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return HeroSkinActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		HeroSkinActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HeroSkinActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}
}
