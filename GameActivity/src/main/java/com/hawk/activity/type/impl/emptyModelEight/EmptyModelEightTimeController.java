package com.hawk.activity.type.impl.emptyModelEight;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.emptyModelEight.cfg.EmptyModelEightActivityKVCfg;
import com.hawk.activity.type.impl.emptyModelEight.cfg.EmptyModelEightActivityTimeCfg;

public class EmptyModelEightTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return EmptyModelEightActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		EmptyModelEightActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(EmptyModelEightActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
