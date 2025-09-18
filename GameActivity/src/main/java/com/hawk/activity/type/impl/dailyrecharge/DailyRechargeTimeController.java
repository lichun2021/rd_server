package com.hawk.activity.type.impl.dailyrecharge;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.dailyrecharge.cfg.RechargeBuyActivityKVCfg;
import com.hawk.activity.type.impl.dailyrecharge.cfg.RechargeBuyActivityTimeCfg;

public class DailyRechargeTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return RechargeBuyActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		RechargeBuyActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(RechargeBuyActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
