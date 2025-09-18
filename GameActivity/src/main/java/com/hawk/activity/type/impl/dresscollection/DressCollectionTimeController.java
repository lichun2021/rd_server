package com.hawk.activity.type.impl.dresscollection;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.dresscollection.cfg.DressCollectionKVCfg;
import com.hawk.activity.type.impl.dresscollection.cfg.DressCollectionTimeCfg;

public class DressCollectionTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return DressCollectionTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		DressCollectionKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(DressCollectionKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}
}
