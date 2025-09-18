package com.hawk.activity.type.impl.treasury;

import org.hawk.config.HawkConfigBase;

import com.hawk.activity.timeController.impl.ServerOpenTimeController;
import com.hawk.activity.type.impl.treasury.cfg.TreasuryTimeCfg;

public class TreasuryController extends ServerOpenTimeController{

	@Override
	public long getServerDelay() {
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return TreasuryTimeCfg.class;
	}

}
