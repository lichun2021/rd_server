package com.hawk.activity.type.impl.emptyModelSeven;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.emptyModelSeven.cfg.EmptyModelSevenActivityKVCfg;
import com.hawk.activity.type.impl.emptyModelSeven.cfg.EmptyModelSevenActivityTimeCfg;

public class EmptyModelSevenTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return EmptyModelSevenActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		EmptyModelSevenActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(EmptyModelSevenActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
