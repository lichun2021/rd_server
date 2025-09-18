package com.hawk.activity.type.impl.growUpBoost;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.growUpBoost.cfg.GrowUpBoostKVCfg;
import com.hawk.activity.type.impl.growUpBoost.cfg.GrowUpBoostTimeCfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

public class GrowUpBoostTimeController extends ExceptCurrentTermTimeController {
    @Override
    public Class<? extends HawkConfigBase> getTimeCfgClass() {
        return GrowUpBoostTimeCfg.class;
    }

    @Override
    public long getServerDelay() {
    	GrowUpBoostKVCfg config = HawkConfigManager.getInstance().getKVInstance(GrowUpBoostKVCfg.class);
		if (config != null) {
			return config.getServerDelay();
		}
		return 0;
    }
}
