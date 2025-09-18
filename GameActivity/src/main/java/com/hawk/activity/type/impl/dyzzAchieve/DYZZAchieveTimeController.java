package com.hawk.activity.type.impl.dyzzAchieve;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.dyzzAchieve.cfg.DYZZAchieveKVCfg;
import com.hawk.activity.type.impl.dyzzAchieve.cfg.DYZZAchieveTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

public class DYZZAchieveTimeController extends ExceptCurrentTermTimeController {
    @Override
    public Class<? extends HawkConfigBase> getTimeCfgClass() {
        return DYZZAchieveTimeCfg.class;
    }

    @Override
    public long getServerDelay() {
        DYZZAchieveKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(DYZZAchieveKVCfg.class);
        if(cfg != null){
            return cfg.getServerDelay();
        }
        return 0;
    }
}
