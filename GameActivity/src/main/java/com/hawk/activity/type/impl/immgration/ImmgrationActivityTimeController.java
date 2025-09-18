package com.hawk.activity.type.impl.immgration;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.immgration.cfg.ImmgrationActivityKVCfg;
import com.hawk.activity.type.impl.immgration.cfg.ImmgrationActivityTimeCfg;

public class ImmgrationActivityTimeController extends ExceptCurrentTermTimeController {
    @Override
    public long getServerDelay() {
    	ImmgrationActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(ImmgrationActivityKVCfg.class);
        if (cfg != null) {
            return cfg.getServerDelay();
        }
        return 0;
    }

    @Override
    public Class<? extends HawkConfigBase> getTimeCfgClass() {
        return ImmgrationActivityTimeCfg.class;
    }
}
