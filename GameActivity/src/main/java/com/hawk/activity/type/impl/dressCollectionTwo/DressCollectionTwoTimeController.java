package com.hawk.activity.type.impl.dressCollectionTwo;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ServerOpenTimeController;
import com.hawk.activity.type.impl.dressCollectionTwo.cfg.DressCollectionTwoKVCfg;
import com.hawk.activity.type.impl.dressCollectionTwo.cfg.DressCollectionTwoTimeCfg;

public class DressCollectionTwoTimeController extends ServerOpenTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return DressCollectionTwoTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		DressCollectionTwoKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(DressCollectionTwoKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}
}
