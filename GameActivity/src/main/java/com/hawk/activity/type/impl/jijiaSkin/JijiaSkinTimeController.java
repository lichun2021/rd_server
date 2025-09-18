package com.hawk.activity.type.impl.jijiaSkin;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.jijiaSkin.cfg.JijiaSkinActivityKVCfg;
import com.hawk.activity.type.impl.jijiaSkin.cfg.JijiaSkinActivityTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

public class JijiaSkinTimeController extends ExceptCurrentTermTimeController {
    @Override
    public Class<? extends HawkConfigBase> getTimeCfgClass() {
        return JijiaSkinActivityTimeCfg.class;
    }

    @Override
    public long getServerDelay() {
        JijiaSkinActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(JijiaSkinActivityKVCfg.class);
        if(cfg != null){
            return cfg.getServerDelay();
        }
        return 0;
    }
}
