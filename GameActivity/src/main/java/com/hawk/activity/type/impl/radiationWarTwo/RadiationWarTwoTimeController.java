package com.hawk.activity.type.impl.radiationWarTwo;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.radiationWarTwo.cfg.RadiationWarTwoActivityKVCfg;
import com.hawk.activity.type.impl.radiationWarTwo.cfg.RadiationWarTwoActivityTimeCfg;

public class RadiationWarTwoTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return RadiationWarTwoActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		RadiationWarTwoActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(RadiationWarTwoActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
