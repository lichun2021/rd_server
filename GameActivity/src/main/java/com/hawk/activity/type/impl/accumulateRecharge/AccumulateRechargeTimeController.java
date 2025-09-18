package com.hawk.activity.type.impl.accumulateRecharge;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.accumulateRecharge.cfg.AccumulateRechargeActivityKVCfg;
import com.hawk.activity.type.impl.accumulateRecharge.cfg.AccumulateRechargeActivityTimeCfg;

public class AccumulateRechargeTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return AccumulateRechargeActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		AccumulateRechargeActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(AccumulateRechargeActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
