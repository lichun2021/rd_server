package com.hawk.activity.type.impl.starInvest;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.starInvest.cfg.StarInvestKVCfg;
import com.hawk.activity.type.impl.starInvest.cfg.StarInvestTimeCfg;

public class StarInvestTimeController extends ExceptCurrentTermTimeController {

	public StarInvestTimeController(){
		
	}
	@Override
	public long getServerDelay() {
		StarInvestKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(StarInvestKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return StarInvestTimeCfg.class;
	}
}
