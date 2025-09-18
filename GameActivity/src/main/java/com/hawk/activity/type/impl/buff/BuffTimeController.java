package com.hawk.activity.type.impl.buff;

import org.hawk.config.HawkConfigBase;

import com.hawk.activity.timeController.impl.ServerOpenTimeController;
import com.hawk.activity.type.impl.buff.cfg.ActivityBuffTimeCfg;

public class BuffTimeController extends ServerOpenTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return ActivityBuffTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		return 0;
	}

}
