package com.hawk.activity.type.impl.powerup;

import org.hawk.config.HawkConfigBase;

import com.hawk.activity.timeController.impl.ServerOpenTimeController;
import com.hawk.activity.type.impl.powerup.cfg.PowerUpActivityTimeCfg;

public class PowerUpTimeController extends ServerOpenTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return PowerUpActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		return 0;
	}

}
