package com.hawk.activity.type.impl.spaceguard;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.spaceguard.cfg.SpaceGuardKVCfg;
import com.hawk.activity.type.impl.spaceguard.cfg.SpaceGuardTimeCfg;

public class SpaceGuardTimeController extends ExceptCurrentTermTimeController {

	public SpaceGuardTimeController(){
		
	}
	@Override
	public long getServerDelay() {
		SpaceGuardKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(SpaceGuardKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return SpaceGuardTimeCfg.class;
	}
}
