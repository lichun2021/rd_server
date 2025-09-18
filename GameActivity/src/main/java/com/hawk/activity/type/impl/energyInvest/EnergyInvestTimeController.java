package com.hawk.activity.type.impl.energyInvest;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.energyInvest.cfg.EnergyInvestKVCfg;
import com.hawk.activity.type.impl.energyInvest.cfg.EnergyInvestTimeCfg;
import com.hawk.activity.type.impl.medalFund.cfg.MedalFundTimeCfg;

public class EnergyInvestTimeController extends ExceptCurrentTermTimeController {

	public EnergyInvestTimeController(){
		
	}
	@Override
	public long getServerDelay() {
		EnergyInvestKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(EnergyInvestKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return EnergyInvestTimeCfg.class;
	}
}
