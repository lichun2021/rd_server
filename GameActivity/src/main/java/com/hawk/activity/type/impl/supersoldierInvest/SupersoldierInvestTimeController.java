package com.hawk.activity.type.impl.supersoldierInvest;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.supersoldierInvest.cfg.SupersoldierInvestKVCfg;
import com.hawk.activity.type.impl.supersoldierInvest.cfg.SupersoldierInvestTimeCfg;

public class SupersoldierInvestTimeController extends ExceptCurrentTermTimeController {

	public SupersoldierInvestTimeController(){
		
	}
	@Override
	public long getServerDelay() {
		SupersoldierInvestKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(SupersoldierInvestKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return SupersoldierInvestTimeCfg.class;
	}
}
