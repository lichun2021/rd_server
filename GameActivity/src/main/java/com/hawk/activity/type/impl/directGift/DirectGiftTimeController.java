package com.hawk.activity.type.impl.directGift;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.directGift.cfg.DirectGiftKvCfg;
import com.hawk.activity.type.impl.directGift.cfg.DirectGiftTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

public class DirectGiftTimeController extends ExceptCurrentTermTimeController {
    @Override
    public Class<? extends HawkConfigBase> getTimeCfgClass() {
        return DirectGiftTimeCfg.class;
    }

    @Override
    public long getServerDelay() {
        DirectGiftKvCfg cfg = HawkConfigManager.getInstance().getKVInstance(DirectGiftKvCfg.class);
        if (cfg != null){
            return cfg.getServerDelay();
        }
        return 0l;
    }
}
