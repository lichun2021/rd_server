package com.hawk.activity.type.impl.superDiscount;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.superDiscount.cfg.SuperDiscountKVCfg;
import com.hawk.activity.type.impl.superDiscount.cfg.SuperDiscountTimeCfg;

public class SuperDiscountActivityController extends ExceptCurrentTermTimeController {

	public SuperDiscountActivityController(){
		
	}
	@Override
	public long getServerDelay() {
		SuperDiscountKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(SuperDiscountKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return SuperDiscountTimeCfg.class;
	}
}
