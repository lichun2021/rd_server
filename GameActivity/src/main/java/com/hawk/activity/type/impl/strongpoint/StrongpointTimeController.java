package com.hawk.activity.type.impl.strongpoint;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.strongpoint.cfg.StrongpointActivityKVCfg;
import com.hawk.activity.type.impl.strongpoint.cfg.StrongpointActivityTimeCfg;

public class StrongpointTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return StrongpointActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		StrongpointActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(StrongpointActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
