package com.hawk.activity.type.impl.diffNewServerTech;


import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.diffNewServerTech.cfg.DiffNewServerTechKVCfg;
import com.hawk.activity.type.impl.diffNewServerTech.cfg.DiffNewServerTechTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

public class DiffNewServerTechTimeController  extends ExceptCurrentTermTimeController {
    @Override
    public Class<? extends HawkConfigBase> getTimeCfgClass() {
        return DiffNewServerTechTimeCfg.class;
    }

    @Override
    public long getServerDelay() {
        DiffNewServerTechKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(DiffNewServerTechKVCfg.class);
        if(cfg != null){
            return cfg.getServerDelay();
        }
        return 0;
    }
}
