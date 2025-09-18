package com.hawk.activity.type.impl.accumulateConsume;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.accumulateConsume.cfg.AccumulateConsumeActivityKVCfg;
import com.hawk.activity.type.impl.accumulateConsume.cfg.AccumulateConsumeActivityTimeCfg;

public class AccumulateConsumeTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return AccumulateConsumeActivityTimeCfg.class;
	}

	public long getServerDelay() {
		AccumulateConsumeActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(AccumulateConsumeActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
