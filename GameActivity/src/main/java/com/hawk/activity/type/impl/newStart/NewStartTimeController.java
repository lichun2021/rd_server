package com.hawk.activity.type.impl.newStart;

import com.hawk.activity.timeController.impl.JoinCurrentTermTimeController;
import com.hawk.activity.type.impl.newStart.cfg.NewStartBaseCfg;
import com.hawk.activity.type.impl.newStart.cfg.NewStartTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

public class NewStartTimeController extends JoinCurrentTermTimeController {
    @Override
    public Class<? extends HawkConfigBase> getTimeCfgClass() {
        return NewStartTimeCfg.class;
    }

    @Override
    public long getServerDelay() {
        NewStartBaseCfg baseCfg = HawkConfigManager.getInstance().getKVInstance(NewStartBaseCfg.class);
        if(baseCfg != null){
            return baseCfg.getServerDelay();
        }
        return 0;
    }
}
