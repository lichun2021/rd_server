package com.hawk.activity.type.impl.bannerkill;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.JoinCurrentTermTimeController;
import com.hawk.activity.type.impl.bannerkill.cfg.ActivityBannerKillKVCfg;
import com.hawk.activity.type.impl.bannerkill.cfg.ActivityBannerKillTimeCfg;

public class BannerKillTimeController extends JoinCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return ActivityBannerKillTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		ActivityBannerKillKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(ActivityBannerKillKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
