package com.hawk.activity.type.impl.timeLimitDrop;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.timeLimitDrop.cfg.TimeLimitDropCfg;
import com.hawk.activity.type.impl.timeLimitDrop.cfg.TimeLimitDropTimeCfg;

public class TimeLimitDropTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return TimeLimitDropTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		TimeLimitDropCfg cfg = HawkConfigManager.getInstance().getKVInstance(TimeLimitDropCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
