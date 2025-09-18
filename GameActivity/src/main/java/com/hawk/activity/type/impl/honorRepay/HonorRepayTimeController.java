package com.hawk.activity.type.impl.honorRepay;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.honorRepay.cfg.HonorRepayKVCfg;
import com.hawk.activity.type.impl.honorRepay.cfg.HonorRepayTimeCfg;

public class HonorRepayTimeController extends ExceptCurrentTermTimeController {

    public HonorRepayTimeController(){

    }
    @Override
    public long getServerDelay() {
        HonorRepayKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HonorRepayKVCfg.class);
        if (cfg != null) {
            return cfg.getServerDelay();
        }
        return 0;
    }

    @Override
    public Class<? extends HawkConfigBase> getTimeCfgClass() {
        return HonorRepayTimeCfg.class;
    }
}
