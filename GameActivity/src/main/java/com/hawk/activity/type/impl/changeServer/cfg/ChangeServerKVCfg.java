package com.hawk.activity.type.impl.changeServer.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "activity/changeserver/changeserver_const.xml")
public class ChangeServerKVCfg extends HawkConfigBase {
    private final int serverDelay;
    private final int costRankNothing ;
    private final int costPowerNothing ;

    public ChangeServerKVCfg(){
        serverDelay = 0;
        costRankNothing = 200;
        costPowerNothing = 200;
    }

    public long getServerDelay() {
        return serverDelay * 1000l;
    }

    public int getCostRankNothing() {
        return costRankNothing;
    }

    public int getCostPowerNothing() {
        return costPowerNothing;
    }
}
