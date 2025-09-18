package com.hawk.activity.type.impl.festival;

import org.hawk.config.HawkConfigBase;

import com.hawk.activity.timeController.impl.RegisterTimeController;
import com.hawk.activity.type.impl.festival.cfg.FestivalActivityTimeCfg;

public class FestivalTimeController extends RegisterTimeController {
	
	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return FestivalActivityTimeCfg.class;
	}
}
