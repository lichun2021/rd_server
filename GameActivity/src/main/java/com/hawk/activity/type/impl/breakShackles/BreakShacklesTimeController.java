package com.hawk.activity.type.impl.breakShackles;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.JoinCurrentTermTimeController;
import com.hawk.activity.type.impl.breakShackles.cfg.BreakShacklesKVCfg;
import com.hawk.activity.type.impl.breakShackles.cfg.BreakShacklesTimeCfg;

public class BreakShacklesTimeController extends JoinCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return BreakShacklesTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		BreakShacklesKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(BreakShacklesKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
