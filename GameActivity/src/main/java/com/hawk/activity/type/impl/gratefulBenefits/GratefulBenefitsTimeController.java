package com.hawk.activity.type.impl.gratefulBenefits;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.gratefulBenefits.cfg.GratefulBenefitsKVCfg;
import com.hawk.activity.type.impl.gratefulBenefits.cfg.GratefulBenefitsTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 感恩福利时间管理类
 */
public class GratefulBenefitsTimeController extends ExceptCurrentTermTimeController {
    /**
     * 获得活动时间配置
     * @return
     */
    @Override
    public Class<? extends HawkConfigBase> getTimeCfgClass() {
        return GratefulBenefitsTimeCfg.class;
    }

    /**
     * 开服多久后可以开启活动
     * @return
     */
    @Override
    public long getServerDelay() {
        //获得活动基础配置
        GratefulBenefitsKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(GratefulBenefitsKVCfg.class);
        //获得开服延迟时间
        if(cfg != null){
            return cfg.getServerDelay();
        }
        return 0;
    }
}
