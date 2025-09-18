package com.hawk.activity.type.impl.bountyHunter;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.bountyHunter.config.BountyHunterActivityKVCfg;
import com.hawk.activity.type.impl.bountyHunter.config.BountyHunterActivityTimeCfg;

public class BountyHunterTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return BountyHunterActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		BountyHunterActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(BountyHunterActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}
}
