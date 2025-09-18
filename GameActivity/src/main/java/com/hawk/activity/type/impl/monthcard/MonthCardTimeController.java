package com.hawk.activity.type.impl.monthcard;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.JoinCurrentTermTimeController;
import com.hawk.activity.type.impl.monthcard.cfg.MonthCardActivityKVCfg;
import com.hawk.activity.type.impl.monthcard.cfg.MonthCardActivityTimeCfg;

public class MonthCardTimeController extends JoinCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return MonthCardActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		MonthCardActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(MonthCardActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
