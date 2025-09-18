package com.hawk.activity.type.impl.emptyModelFive;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.emptyModelFive.cfg.EmptyModelFiveActivityKVCfg;
import com.hawk.activity.type.impl.emptyModelFive.cfg.EmptyModelFiveActivityTimeCfg;

public class EmptyModelFiveTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return EmptyModelFiveActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		EmptyModelFiveActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(EmptyModelFiveActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
