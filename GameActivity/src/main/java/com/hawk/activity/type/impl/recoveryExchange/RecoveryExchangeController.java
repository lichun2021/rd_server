package com.hawk.activity.type.impl.recoveryExchange;

import com.hawk.activity.type.impl.recoveryExchange.cfg.RecoveryExchangeKVCfg;
import com.hawk.activity.type.impl.recoveryExchange.cfg.RecoveryExchangeTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;

/**
 * 道具回收活动
 *
 * @author richard
 */
public class RecoveryExchangeController extends ExceptCurrentTermTimeController {

    @Override
    public Class<? extends HawkConfigBase> getTimeCfgClass() {
        return RecoveryExchangeTimeCfg.class;
    }

    @Override
    public long getServerDelay() {
        RecoveryExchangeKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(RecoveryExchangeKVCfg.class);
        if (cfg != null) {
            return cfg.getServerDelay();
        }
        return 0;
    }
}
