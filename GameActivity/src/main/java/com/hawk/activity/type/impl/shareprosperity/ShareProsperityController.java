package com.hawk.activity.type.impl.shareprosperity;

import com.hawk.activity.timeController.impl.ServerOpenTimeController;
import com.hawk.activity.type.impl.shareprosperity.cfg.ShareProsperityKVCfg;
import com.hawk.activity.type.impl.shareprosperity.cfg.ShareProsperityTimeCfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

public class ShareProsperityController extends ServerOpenTimeController {
	
    @Override
    public Class<? extends HawkConfigBase> getTimeCfgClass() {
        return ShareProsperityTimeCfg.class;
    }

    @Override
    public long getServerDelay() {
    	ShareProsperityKVCfg baseCfg = HawkConfigManager.getInstance().getKVInstance(ShareProsperityKVCfg.class);
        if(baseCfg != null){
            return baseCfg.getServerDelay();
        }
        return 0;
    }
    
}
