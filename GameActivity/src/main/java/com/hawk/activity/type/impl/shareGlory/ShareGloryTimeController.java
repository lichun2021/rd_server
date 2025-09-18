package com.hawk.activity.type.impl.shareGlory;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.shareGlory.cfg.ShareGloryKVCfg;
import com.hawk.activity.type.impl.shareGlory.cfg.ShareGloryTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;


/**
 * 荣耀同享活动时间控制配置
 *
 * @author richard
 */
public class ShareGloryTimeController extends ExceptCurrentTermTimeController {

    @Override
    public Class<? extends HawkConfigBase> getTimeCfgClass() {
        return ShareGloryTimeCfg.class;
    }

    @Override
    public long getServerDelay() {
        ShareGloryKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(ShareGloryKVCfg.class);
        if (cfg != null) {
            return cfg.getServerDelay();
        }
        return 0;
    }

}
