package com.hawk.activity.type.impl.heroLove;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.heroLove.cfg.HeroLoveActivityKVCfg;
import com.hawk.activity.type.impl.heroLove.cfg.HeroLoveTimeCfg;

public class HeroLoveTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return HeroLoveTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		HeroLoveActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HeroLoveActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
