package com.hawk.activity.type.impl.pddActivity;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.pddActivity.cfg.PDDKVCfg;
import com.hawk.activity.type.impl.pddActivity.cfg.PDDTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

public class PDDActivityTimeController extends ExceptCurrentTermTimeController {

    @Override
    public Class<? extends HawkConfigBase> getTimeCfgClass() {
        return PDDTimeCfg.class;
    }

    @Override
    public long getServerDelay() {
        PDDKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PDDKVCfg.class);
        if(cfg != null){
            return cfg.getServerDelay();
        }
        return 0;
    }
}
