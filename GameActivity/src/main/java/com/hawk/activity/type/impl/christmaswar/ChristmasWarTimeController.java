package com.hawk.activity.type.impl.christmaswar;

import org.hawk.config.HawkConfigBase;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.christmaswar.cfg.ChristmasWarKVCfg;
import com.hawk.activity.type.impl.christmaswar.cfg.ChristmasWarTimeCfg;

public class ChristmasWarTimeController extends ExceptCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		return ChristmasWarKVCfg.getInstance().getServerDelay();
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return ChristmasWarTimeCfg.class;
	}

}
