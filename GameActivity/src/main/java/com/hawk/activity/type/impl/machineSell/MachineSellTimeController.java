package com.hawk.activity.type.impl.machineSell;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.machineSell.cfg.MachineSellActivityTimeCfg;
import com.hawk.activity.type.impl.machineSell.cfg.MachineSellKVCfg;

public class MachineSellTimeController extends ExceptCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		MachineSellKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(MachineSellKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return MachineSellActivityTimeCfg.class;
	}
}
