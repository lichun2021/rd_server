package com.hawk.activity.type.impl.developFastOld;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.timeController.impl.ServerOpenTimeController;
import com.hawk.activity.type.impl.developFastOld.cfg.DevelopFastOldTimeCfg;
import org.hawk.config.HawkConfigBase;

/**
 * 实力飞升 时间控制器
 */
public class DevelopFastOldTimeController extends ExceptCurrentTermTimeController {
    @Override
    public Class<? extends HawkConfigBase> getTimeCfgClass() {
        return DevelopFastOldTimeCfg.class;
    }

    @Override
    public long getServerDelay() {
        return 0;
    }
}
