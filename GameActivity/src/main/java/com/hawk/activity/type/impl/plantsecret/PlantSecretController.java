package com.hawk.activity.type.impl.plantsecret;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.plantsecret.cfg.PlantSecretKVCfg;
import com.hawk.activity.type.impl.plantsecret.cfg.PlantSecretTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;


/**
 * 泰能机密活动
 *
 * @author lating
 */
public class PlantSecretController extends ExceptCurrentTermTimeController {

    @Override
    public Class<? extends HawkConfigBase> getTimeCfgClass() {
        return PlantSecretTimeCfg.class;
    }

    @Override
    public long getServerDelay() {
        PlantSecretKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PlantSecretKVCfg.class);
        if (cfg != null) {
            return cfg.getServerDelay();
        }
        return 0;
    }

}
