package com.hawk.activity.type.impl.growfundnew;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.JoinCurrentTermTimeController;
import com.hawk.activity.type.impl.growfundnew.cfg.GrowFundNewActivityKVCfg;
import com.hawk.activity.type.impl.growfundnew.cfg.GrowFundNewActivityTimeCfg;

public class GrowFundNewTimeController extends JoinCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return GrowFundNewActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		GrowFundNewActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(GrowFundNewActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
