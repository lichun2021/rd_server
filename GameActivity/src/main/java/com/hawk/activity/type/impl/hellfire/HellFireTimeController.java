package com.hawk.activity.type.impl.hellfire;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.hellfire.cfg.ActivityHellFireKVCfg;
import com.hawk.activity.type.impl.hellfire.cfg.ActivityHellFireTimeCfg;

public class HellFireTimeController extends ExceptCurrentTermTimeController{

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return ActivityHellFireTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		ActivityHellFireKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(ActivityHellFireKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
