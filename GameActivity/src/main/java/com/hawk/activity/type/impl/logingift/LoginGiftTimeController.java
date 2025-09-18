package com.hawk.activity.type.impl.logingift;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.RegisterTimeController;
import com.hawk.activity.type.impl.logingift.cfg.LoginGiftActivityKVCfg;
import com.hawk.activity.type.impl.logingift.cfg.LoginGiftActivityTimeCfg;

public class LoginGiftTimeController extends RegisterTimeController {

	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return LoginGiftActivityTimeCfg.class;
	}

	public long getServerDelay() {
		LoginGiftActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(LoginGiftActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}
	
}
