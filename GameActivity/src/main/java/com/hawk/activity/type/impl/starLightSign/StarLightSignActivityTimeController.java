package com.hawk.activity.type.impl.starLightSign;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.starLightSign.cfg.StarLightSignKVCfg;
import com.hawk.activity.type.impl.starLightSign.cfg.StarLightSignTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

public class StarLightSignActivityTimeController extends ExceptCurrentTermTimeController {
    @Override
    public Class<? extends HawkConfigBase> getTimeCfgClass() {
        return StarLightSignTimeCfg.class;
    }

    @Override
    public long getServerDelay() {
        StarLightSignKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(StarLightSignKVCfg.class);
        if(cfg == null){
            return 0;
        }
        return cfg.getServerDelay();
    }
}
