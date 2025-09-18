package com.hawk.activity.type.impl.developFast;

import com.hawk.activity.timeController.impl.ServerOpenTimeController;
import com.hawk.activity.type.impl.developFast.cfg.DevelopFastTimeCfg;
import org.hawk.config.HawkConfigBase;

/**
 * 实力飞升 时间控制器
 */
public class DevelopFastTimeController extends ServerOpenTimeController {
    @Override
    public Class<? extends HawkConfigBase> getTimeCfgClass() {
        return DevelopFastTimeCfg.class;
    }

    @Override
    public long getServerDelay() {
        return 0;
    }
}
