package com.hawk.activity.type.impl.greatGift;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.greatGift.cfg.GreatGiftActivityKVCfg;
import com.hawk.activity.type.impl.greatGift.cfg.GreatGiftActivityTimeCfg;

public class GreatGiftTimeController extends ExceptCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		GreatGiftActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(GreatGiftActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return GreatGiftActivityTimeCfg.class;
	}

}
