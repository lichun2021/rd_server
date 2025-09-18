package com.hawk.activity.type.impl.continuousRecharge;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.continuousRecharge.cfg.ContinuousRechargeActivityKVCfg;
import com.hawk.activity.type.impl.continuousRecharge.cfg.ContinuousRechargeActivityTimeCfg;


public class ContinuousRechargeTimeController extends ExceptCurrentTermTimeController {
	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return ContinuousRechargeActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		ContinuousRechargeActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(ContinuousRechargeActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}
}
