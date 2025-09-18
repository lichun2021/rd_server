package com.hawk.activity.type.impl.guildbanner;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.JoinCurrentTermTimeController;
import com.hawk.activity.type.impl.guildbanner.cfg.ActivityGuildBannerKVCfg;
import com.hawk.activity.type.impl.guildbanner.cfg.ActivityGuildBannerTimeCfg;

public class GuildBannerTimeController extends JoinCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return ActivityGuildBannerTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		ActivityGuildBannerKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(ActivityGuildBannerKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
