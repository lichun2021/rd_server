package com.hawk.activity.type.impl.hellfiretwo;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.hellfiretwo.cfg.ActivityHellFireTwoKVCfg;
import com.hawk.activity.type.impl.hellfiretwo.cfg.ActivityHellFireTwoTimeCfg;

public class HellFireTwoTimeController extends ExceptCurrentTermTimeController{

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return ActivityHellFireTwoTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		ActivityHellFireTwoKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(ActivityHellFireTwoKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
