package com.hawk.activity.type.impl.honourMobilize;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ServerOpenTimeController;
import com.hawk.activity.type.impl.honourMobilize.cfg.HonourMobilizeKVCfg;
import com.hawk.activity.type.impl.honourMobilize.cfg.HonourMobilizeTimeCfg;


/**
 * 
 * @author che
 *
 */
public class HonourMobilizeTimeController extends ServerOpenTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return HonourMobilizeTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		HonourMobilizeKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HonourMobilizeKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
