package com.hawk.activity.type.impl.directGift.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "activity/direct_gift/direct_gift_cfg.xml")
public class DirectGiftKvCfg extends HawkConfigBase {

    private final int serverDelay;

    public DirectGiftKvCfg(){
        serverDelay = 0;
    }

    public long getServerDelay() {
        return serverDelay * 1000l;
    }
}
