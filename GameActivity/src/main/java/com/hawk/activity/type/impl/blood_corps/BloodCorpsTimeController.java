package com.hawk.activity.type.impl.blood_corps;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.blood_corps.cfg.BloodCorpsActivityKVCfg;
import com.hawk.activity.type.impl.blood_corps.cfg.BloodCorpsActivityTimeCfg;

public class BloodCorpsTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return BloodCorpsActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		BloodCorpsActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(BloodCorpsActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
