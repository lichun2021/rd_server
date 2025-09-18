package com.hawk.activity.type.impl.medalFund;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.medalFund.cfg.MedalFundKVCfg;
import com.hawk.activity.type.impl.medalFund.cfg.MedalFundTimeCfg;

public class MedalFundTimeController extends ExceptCurrentTermTimeController {

	public MedalFundTimeController(){
		
	}
	@Override
	public long getServerDelay() {
		MedalFundKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(MedalFundKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return MedalFundTimeCfg.class;
	}
}
