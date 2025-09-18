package com.hawk.activity.type.impl.loginday;

import org.hawk.config.HawkConfigBase;

import com.hawk.activity.timeController.impl.RegisterTimeController;
import com.hawk.activity.type.impl.loginday.cfg.LoginDayActivityTimeCfg;

public class LoginDayTimeController extends RegisterTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return LoginDayActivityTimeCfg.class;
	}
}
