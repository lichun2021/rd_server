package com.hawk.activity.type.impl.allyBeatBack;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.allyBeatBack.cfg.AllyBeatBackCfg;
import com.hawk.activity.type.impl.allyBeatBack.cfg.AllyBeatBackTimeCfg;

public class AllyBeatBackTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return AllyBeatBackTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		AllyBeatBackCfg cfg = HawkConfigManager.getInstance().getKVInstance(AllyBeatBackCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
