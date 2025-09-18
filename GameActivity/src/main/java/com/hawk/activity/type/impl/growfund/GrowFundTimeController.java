package com.hawk.activity.type.impl.growfund;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.JoinCurrentTermTimeController;
import com.hawk.activity.type.impl.growfund.cfg.GrowFundActivityKVCfg;
import com.hawk.activity.type.impl.growfund.cfg.GrowFundActivityTimeCfg;

public class GrowFundTimeController extends JoinCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return GrowFundActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		GrowFundActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(GrowFundActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
