package com.hawk.activity.type.impl.samuraiBlackened;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.samuraiBlackened.cfg.SamuraiBlackenedActivityKVCfg;
import com.hawk.activity.type.impl.samuraiBlackened.cfg.SamuraiBlackenedTimeCfg;

public class SamuraiBlackenedTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return SamuraiBlackenedTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		SamuraiBlackenedActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(SamuraiBlackenedActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
