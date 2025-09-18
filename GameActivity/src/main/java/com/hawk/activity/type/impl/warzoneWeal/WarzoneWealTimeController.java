package com.hawk.activity.type.impl.warzoneWeal;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.warzoneWeal.cfg.WarzoneWealActivityKVCfg;
import com.hawk.activity.type.impl.warzoneWeal.cfg.WarzoneWealActivityTimeCfg;

public class WarzoneWealTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return WarzoneWealActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		WarzoneWealActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(WarzoneWealActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
