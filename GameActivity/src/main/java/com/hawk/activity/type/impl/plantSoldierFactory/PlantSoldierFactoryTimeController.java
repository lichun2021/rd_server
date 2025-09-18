package com.hawk.activity.type.impl.plantSoldierFactory;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.plantSoldierFactory.cfg.PlantSoldierFactoryKVCfg;
import com.hawk.activity.type.impl.plantSoldierFactory.cfg.PlantSoldierFactoryTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

public class PlantSoldierFactoryTimeController extends ExceptCurrentTermTimeController {
    @Override
    public Class<? extends HawkConfigBase> getTimeCfgClass() {
        return PlantSoldierFactoryTimeCfg.class;
    }

    @Override
    public long getServerDelay() {
        PlantSoldierFactoryKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PlantSoldierFactoryKVCfg.class);
        if(cfg != null){
            return cfg.getServerDelay();
        }
        return 0;
    }
}
