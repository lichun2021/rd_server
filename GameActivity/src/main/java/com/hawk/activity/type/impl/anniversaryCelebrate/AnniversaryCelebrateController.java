package com.hawk.activity.type.impl.anniversaryCelebrate;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.anniversaryCelebrate.cfg.AnniversaryCelebrateKVCfg;
import com.hawk.activity.type.impl.anniversaryCelebrate.cfg.AnniversaryCelebrateTimeCfg;

public class AnniversaryCelebrateController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return AnniversaryCelebrateTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		AnniversaryCelebrateKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(AnniversaryCelebrateKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
