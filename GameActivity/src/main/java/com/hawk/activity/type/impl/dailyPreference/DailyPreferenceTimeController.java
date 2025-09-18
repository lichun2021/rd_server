package com.hawk.activity.type.impl.dailyPreference;

import org.hawk.config.HawkConfigBase;

import com.hawk.activity.timeController.impl.ServerOpenTimeController;
import com.hawk.activity.type.impl.dailyPreference.cfg.DailyPreferenceActivityTimeCfg;

public class DailyPreferenceTimeController extends ServerOpenTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return DailyPreferenceActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		return 0;
	}


}
