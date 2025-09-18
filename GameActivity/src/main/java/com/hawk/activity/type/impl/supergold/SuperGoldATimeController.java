package com.hawk.activity.type.impl.supergold;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.supergold.cfg.SuperGoldKVCfg;
import com.hawk.activity.type.impl.supergold.cfg.SuperGoldTimeCfg;

public class SuperGoldATimeController extends ExceptCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		SuperGoldKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(SuperGoldKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return SuperGoldTimeCfg.class;
	}

}
