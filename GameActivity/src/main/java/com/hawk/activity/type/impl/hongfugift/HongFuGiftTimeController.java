package com.hawk.activity.type.impl.hongfugift;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.hongfugift.cfg.HongFuGiftActivityKVCfg;
import com.hawk.activity.type.impl.hongfugift.cfg.HongFuGiftActivityTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/** 洪福礼包时间控制器
 * @author hf
 */
public class HongFuGiftTimeController extends ExceptCurrentTermTimeController {
    @Override
    public long getServerDelay() {
        HongFuGiftActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HongFuGiftActivityKVCfg.class);
        if (cfg != null) {
            return cfg.getServerDelay();
        }
        return 0;
    }

    @Override
    public Class<? extends HawkConfigBase> getTimeCfgClass() {
        return HongFuGiftActivityTimeCfg.class;
    }

}
