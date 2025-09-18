package com.hawk.activity.type.impl.powercollect;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.powercollect.cfg.PowerCollectKVCfg;
import com.hawk.activity.type.impl.powercollect.cfg.PowerCollectTimeCfg;

public class PowerCollectTimeController extends ExceptCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		PowerCollectKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PowerCollectKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return PowerCollectTimeCfg.class;
	}

}
