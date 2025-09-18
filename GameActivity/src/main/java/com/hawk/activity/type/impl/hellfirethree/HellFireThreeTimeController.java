package com.hawk.activity.type.impl.hellfirethree;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.hellfirethree.cfg.ActivityHellFireThreeKVCfg;
import com.hawk.activity.type.impl.hellfirethree.cfg.ActivityHellFireThreeTimeCfg;

public class HellFireThreeTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return ActivityHellFireThreeTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		ActivityHellFireThreeKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(ActivityHellFireThreeKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
