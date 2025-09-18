package com.hawk.activity.type.impl.strongestGuild;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.strongestGuild.cfg.StrongestGuildActivityTimeCfg;
import com.hawk.activity.type.impl.strongestGuild.cfg.StrongestGuildKVCfg;

public class StrongestGuildTimeController extends ExceptCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		StrongestGuildKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(StrongestGuildKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return StrongestGuildActivityTimeCfg.class;
	}

}
