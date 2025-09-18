package com.hawk.activity.type.impl.supplyCrate;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.supplyCrate.cfg.SupplyCrateKVCfg;
import com.hawk.activity.type.impl.supplyCrate.cfg.SupplyCrateTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

public class SupplyCrateTimeController extends ExceptCurrentTermTimeController {
    @Override
    public Class<? extends HawkConfigBase> getTimeCfgClass() {
        return SupplyCrateTimeCfg.class;
    }

    @Override
    public long getServerDelay() {
        SupplyCrateKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(SupplyCrateKVCfg.class);
        if(cfg != null){
            return cfg.getServerDelay();
        }
        return 0;
    }
}
