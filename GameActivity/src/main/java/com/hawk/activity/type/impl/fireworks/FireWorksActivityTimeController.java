package com.hawk.activity.type.impl.fireworks;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.fireworks.cfg.FireWorksKVCfg;
import com.hawk.activity.type.impl.fireworks.cfg.FireWorksTimeCfg;

public class FireWorksActivityTimeController extends ExceptCurrentTermTimeController {

	public FireWorksActivityTimeController(){
		
	}
	@Override
	public long getServerDelay() {
		FireWorksKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(FireWorksKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return FireWorksTimeCfg.class;
	}
}
