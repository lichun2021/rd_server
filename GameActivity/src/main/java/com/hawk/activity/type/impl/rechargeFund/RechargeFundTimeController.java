package com.hawk.activity.type.impl.rechargeFund;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.rechargeFund.cfg.RechargeFundKVCfg;
import com.hawk.activity.type.impl.rechargeFund.cfg.RechargeFundTimeCfg;

public class RechargeFundTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return RechargeFundTimeCfg.class;
	}
	
	@Override
	public long getServerDelay() {
		RechargeFundKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(RechargeFundKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
