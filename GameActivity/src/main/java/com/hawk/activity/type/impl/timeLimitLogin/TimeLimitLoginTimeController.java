package com.hawk.activity.type.impl.timeLimitLogin;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.timeLimitLogin.cfg.TimeLimitLoginKVCfg;
import com.hawk.activity.type.impl.timeLimitLogin.cfg.TimeLimitLoginTimeCfg;

public class TimeLimitLoginTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return TimeLimitLoginTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		TimeLimitLoginKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(TimeLimitLoginKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
