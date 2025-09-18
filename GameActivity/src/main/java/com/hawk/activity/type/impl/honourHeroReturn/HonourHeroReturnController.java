package com.hawk.activity.type.impl.honourHeroReturn;

import com.hawk.activity.type.impl.honourHeroReturn.cfg.HonourHeroReturnKVCfg;
import com.hawk.activity.type.impl.honourHeroReturn.cfg.HonourHeroReturnTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;

/**
 * 荣耀英雄回归
 *
 * @author richard
 */
public class HonourHeroReturnController extends ExceptCurrentTermTimeController {

    @Override
    public Class<? extends HawkConfigBase> getTimeCfgClass() {
        return HonourHeroReturnTimeCfg.class;
    }

    @Override
    public long getServerDelay() {
        HonourHeroReturnKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HonourHeroReturnKVCfg.class);
        if (cfg != null) {
            return cfg.getServerDelay();
        }
        return 0;
    }

}
