package com.hawk.activity.type.impl.appointget;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.appointget.cfg.AppointGetActivityTimeCfg;
import com.hawk.activity.type.impl.appointget.cfg.AppointGetKVCfg;

public class AppointGetActivityTimeController extends ExceptCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		AppointGetKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(AppointGetKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return AppointGetActivityTimeCfg.class;
	}
}
