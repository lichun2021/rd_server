package com.hawk.activity.type.impl.logindayTwo;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.logindayTwo.cfg.LoginDayTwoActivityTimeCfg;
import com.hawk.activity.type.impl.logindayTwo.cfg.LoginDayTwoKVCfg;

public class LoginDayTwoTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return LoginDayTwoActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		LoginDayTwoKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(LoginDayTwoKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}
}
