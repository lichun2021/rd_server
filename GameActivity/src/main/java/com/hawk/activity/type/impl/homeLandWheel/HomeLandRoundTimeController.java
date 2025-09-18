package com.hawk.activity.type.impl.homeLandWheel;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.homeLandWheel.cfg.HomeLandRoundActivityKVCfg;
import com.hawk.activity.type.impl.homeLandWheel.cfg.HomeLandRoundActivityTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

public class HomeLandRoundTimeController extends ExceptCurrentTermTimeController {
    @Override
    public long getServerDelay() {
        HomeLandRoundActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HomeLandRoundActivityKVCfg.class);
        if (cfg != null) {
            return cfg.getServerDelay();
        }
        return 0;
    }

    @Override
    public Class<? extends HawkConfigBase> getTimeCfgClass() {
        return HomeLandRoundActivityTimeCfg.class;
    }

}

