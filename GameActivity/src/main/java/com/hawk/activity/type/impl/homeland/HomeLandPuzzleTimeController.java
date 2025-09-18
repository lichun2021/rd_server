package com.hawk.activity.type.impl.homeland;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.homeland.cfg.HomeLandPuzzleActivityKVCfg;
import com.hawk.activity.type.impl.homeland.cfg.HomeLandPuzzleActivityTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

public class HomeLandPuzzleTimeController extends ExceptCurrentTermTimeController {
    @Override
    public long getServerDelay() {
        HomeLandPuzzleActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HomeLandPuzzleActivityKVCfg.class);
        if (cfg != null) {
            return cfg.getServerDelay();
        }
        return 0;
    }

    @Override
    public Class<? extends HawkConfigBase> getTimeCfgClass() {
        return HomeLandPuzzleActivityTimeCfg.class;
    }

}

