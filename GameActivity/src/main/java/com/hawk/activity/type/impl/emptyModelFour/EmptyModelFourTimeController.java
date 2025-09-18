package com.hawk.activity.type.impl.emptyModelFour;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.emptyModelFour.cfg.EmptyModelFourActivityKVCfg;
import com.hawk.activity.type.impl.emptyModelFour.cfg.EmptyModelFourActivityTimeCfg;

public class EmptyModelFourTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return EmptyModelFourActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		EmptyModelFourActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(EmptyModelFourActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
