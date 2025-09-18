package com.hawk.activity.type.impl.loginsign;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.JoinCurrentTermTimeController;
import com.hawk.activity.type.impl.loginsign.cfg.LoginSignActivityKVCfg;
import com.hawk.activity.type.impl.loginsign.cfg.LoginSignActivityTimeCfg;

public class LoginSignTimeController extends JoinCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return LoginSignActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		LoginSignActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(LoginSignActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
