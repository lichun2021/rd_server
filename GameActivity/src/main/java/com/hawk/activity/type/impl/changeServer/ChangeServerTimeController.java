package com.hawk.activity.type.impl.changeServer;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.changeServer.cfg.ChangeServerKVCfg;
import com.hawk.activity.type.impl.changeServer.cfg.ChangeServerTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

public class ChangeServerTimeController extends ExceptCurrentTermTimeController {
    @Override
    public Class<? extends HawkConfigBase> getTimeCfgClass() {
        return ChangeServerTimeCfg.class;
    }

    @Override
    public long getServerDelay() {
        ChangeServerKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(ChangeServerKVCfg.class);
        if(cfg != null){
            return cfg.getServerDelay();
        }
        return 0;
    }
}
