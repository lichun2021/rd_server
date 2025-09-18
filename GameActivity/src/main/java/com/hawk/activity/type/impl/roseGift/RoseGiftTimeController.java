package com.hawk.activity.type.impl.roseGift;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.roseGift.cfg.RoseGiftKVCfg;
import com.hawk.activity.type.impl.roseGift.cfg.RoseGiftTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 玫瑰赠礼时间管理类
 */
public class RoseGiftTimeController extends ExceptCurrentTermTimeController {

    /**
     * 关联时间配置
     * @return 时间配置类
     */
    @Override
    public Class<? extends HawkConfigBase> getTimeCfgClass() {
        return RoseGiftTimeCfg.class;
    }

    /**
     * 获得新服屏蔽时间
     * @return
     */
    @Override
    public long getServerDelay() {
        RoseGiftKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(RoseGiftKVCfg.class);
        if(cfg != null){
            return cfg.getServerDelay();
        }
        return 0;
    }
}
