package com.hawk.activity.type.impl.luckGetGold;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.luckGetGold.cfg.LuckGetGoldKVCfg;
import com.hawk.activity.type.impl.luckGetGold.cfg.LuckGetGoldTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

public class LuckGetGoldTimeController extends ExceptCurrentTermTimeController {
    @Override
    public Class<? extends HawkConfigBase> getTimeCfgClass() {
        return LuckGetGoldTimeCfg.class;
    }

    @Override
    public long getServerDelay() {
        LuckGetGoldKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(LuckGetGoldKVCfg.class);
        if(cfg != null){
            return cfg.getServerDelay();
        }
        return 0;
    }
}
