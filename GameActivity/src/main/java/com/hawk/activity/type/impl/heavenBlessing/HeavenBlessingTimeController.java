package com.hawk.activity.type.impl.heavenBlessing;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.heavenBlessing.cfg.HeavenBlessingKVCfg;
import com.hawk.activity.type.impl.heavenBlessing.cfg.HeavenBlessingTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 鸿福天降活动时间管理类
 */
public class HeavenBlessingTimeController extends ExceptCurrentTermTimeController {

    /**
     * 获得活动时间配置
     * @return
     */
    @Override
    public Class<? extends HawkConfigBase> getTimeCfgClass() {
        return HeavenBlessingTimeCfg.class;
    }

    /**
     * 开服多久后可以开启活动
     * @return
     */
    @Override
    public long getServerDelay() {
        //获得活动基础配置
        HeavenBlessingKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HeavenBlessingKVCfg.class);
        //获得开服延迟时间
        if(cfg != null){
            return cfg.getServerDelay();
        }
        return 0;
    }
}
