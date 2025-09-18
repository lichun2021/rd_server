package com.hawk.activity.type.impl.heroTrial;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.JoinCurrentTermTimeController;
import com.hawk.activity.type.impl.heroTrial.cfg.HeroTrialActivityKVCfg;
import com.hawk.activity.type.impl.heroTrial.cfg.HeroTrialActivityTimeCfg;

public class HeroTrialTimeController extends JoinCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		HeroTrialActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HeroTrialActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return HeroTrialActivityTimeCfg.class;
	}

}
