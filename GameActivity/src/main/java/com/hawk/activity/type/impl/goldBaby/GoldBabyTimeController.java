package com.hawk.activity.type.impl.goldBaby;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.goldBaby.cfg.GoldBabyKVCfg;
import com.hawk.activity.type.impl.goldBaby.cfg.GoldBabyTimeCfg;

public class GoldBabyTimeController extends ExceptCurrentTermTimeController{

	public GoldBabyTimeController(){
		
	}
	
	@Override
	public long getServerDelay() {
		GoldBabyKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(GoldBabyKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return GoldBabyTimeCfg.class;
	}

}
