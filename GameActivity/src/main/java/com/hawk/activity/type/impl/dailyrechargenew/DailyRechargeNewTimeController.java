package com.hawk.activity.type.impl.dailyrechargenew;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.dailyrechargenew.cfg.RechargeBuyNewActivityKVCfg;
import com.hawk.activity.type.impl.dailyrechargenew.cfg.RechargeBuyNewActivityTimeCfg;

public class DailyRechargeNewTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return RechargeBuyNewActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		RechargeBuyNewActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(RechargeBuyNewActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
