package com.hawk.activity.type.impl.emptyModelSix;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.emptyModelSix.cfg.EmptyModelSixActivityKVCfg;
import com.hawk.activity.type.impl.emptyModelSix.cfg.EmptyModelSixActivityTimeCfg;

public class EmptyModelSixTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return EmptyModelSixActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		EmptyModelSixActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(EmptyModelSixActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}
}
