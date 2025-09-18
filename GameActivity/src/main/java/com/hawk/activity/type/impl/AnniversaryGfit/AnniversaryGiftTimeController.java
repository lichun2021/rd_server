package com.hawk.activity.type.impl.AnniversaryGfit;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.AnniversaryGfit.cfg.AnniversaryGiftKVCfg;
import com.hawk.activity.type.impl.AnniversaryGfit.cfg.AnniversaryGiftTimeCfg;

public class AnniversaryGiftTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return AnniversaryGiftTimeCfg.class;
	}

	public long getServerDelay() {
		AnniversaryGiftKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(AnniversaryGiftKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
