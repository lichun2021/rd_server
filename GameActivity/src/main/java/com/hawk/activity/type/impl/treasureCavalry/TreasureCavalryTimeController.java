package com.hawk.activity.type.impl.treasureCavalry;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.treasureCavalry.cfg.TreasureCavalryActivityKVCfg;
import com.hawk.activity.type.impl.treasureCavalry.cfg.TreasureCavalryActivityTimeCfg;

public class TreasureCavalryTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return TreasureCavalryActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		TreasureCavalryActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(TreasureCavalryActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}
}
