package com.hawk.activity.type.impl.warFlagTwo;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.warFlagTwo.cfg.WarFlagTwoKVCfg;
import com.hawk.activity.type.impl.warFlagTwo.cfg.WarFlagTwoTimeCfg;

public class WarFlagTwoTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return WarFlagTwoTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		WarFlagTwoKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(WarFlagTwoKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
