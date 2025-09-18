package com.hawk.activity.type.impl.doubleRecharge;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.doubleRecharge.cfg.DoubleRechargeActivityKVCfg;
import com.hawk.activity.type.impl.doubleRecharge.cfg.DoubleRechargeActivityTimeCfg;

public class DoubleRechargeTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return DoubleRechargeActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		DoubleRechargeActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(DoubleRechargeActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
