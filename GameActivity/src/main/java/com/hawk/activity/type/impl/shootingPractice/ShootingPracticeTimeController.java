package com.hawk.activity.type.impl.shootingPractice;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.shootingPractice.cfg.ShootingPracticeKVCfg;
import com.hawk.activity.type.impl.shootingPractice.cfg.ShootingPracticeTimeCfg;

public class ShootingPracticeTimeController extends ExceptCurrentTermTimeController {
    @Override
    public Class<? extends HawkConfigBase> getTimeCfgClass() {
        return ShootingPracticeTimeCfg.class;
    }

    @Override
    public long getServerDelay() {
    	ShootingPracticeKVCfg config = HawkConfigManager.getInstance().getKVInstance(ShootingPracticeKVCfg.class);
		if (config != null) {
			return config.getServerDelay();
		}
		return 0;
    }
}
