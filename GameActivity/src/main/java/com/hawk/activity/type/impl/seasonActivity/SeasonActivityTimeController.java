package com.hawk.activity.type.impl.seasonActivity;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.seasonActivity.cfg.SeasonOpenTimeCfg;
import org.hawk.config.HawkConfigBase;

/**
 * 赛季活动事件管理类
 */
public class SeasonActivityTimeController extends ExceptCurrentTermTimeController {
    /**
     * 时间配置
     * @return
     */
    @Override
    public Class<? extends HawkConfigBase> getTimeCfgClass() {
        return SeasonOpenTimeCfg.class;
    }

    /**
     * 延后开放
     * @return
     */
    @Override
    public long getServerDelay() {
        return 0;
    }
}
