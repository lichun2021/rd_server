package com.hawk.activity.type.impl.planetexploration;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.planetexploration.cfg.PlanetExploreKVCfg;
import com.hawk.activity.type.impl.planetexploration.cfg.PlanetExploreTimeCfg;

public class PlanetExploreTimeController extends ExceptCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		PlanetExploreKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PlanetExploreKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return PlanetExploreTimeCfg.class;
	}

}
