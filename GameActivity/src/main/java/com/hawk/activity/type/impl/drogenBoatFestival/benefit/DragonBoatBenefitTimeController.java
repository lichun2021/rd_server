package com.hawk.activity.type.impl.drogenBoatFestival.benefit;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.drogenBoatFestival.benefit.cfg.DragonBoatBenefitKVCfg;
import com.hawk.activity.type.impl.drogenBoatFestival.benefit.cfg.DragonBoatBenefitTimeCfg;

public class DragonBoatBenefitTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return DragonBoatBenefitTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		DragonBoatBenefitKVCfg cfg = HawkConfigManager.getInstance().
				getKVInstance(DragonBoatBenefitKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
