package com.hawk.activity.type.impl.dyzzAchieve.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "activity/dyzz_achieve/dyzz_achieve_kv_cfg.xml")
public class DYZZAchieveKVCfg extends HawkConfigBase {

    /**
     * 服务器开服延时开启活动时间
     */
    private final int serverDelay;


    private final int achieveHolyShit;
    private final int achieveLessTen;
    private final int achieveEqualOne;

    public DYZZAchieveKVCfg() {
        serverDelay = 0;
        achieveHolyShit = 17;
        achieveLessTen = 10;
        achieveEqualOne = 1;
    }

    public long getServerDelay() {
        return ((long)serverDelay) * 1000;
    }


    public int getAchieveHolyShit() {
        return achieveHolyShit;
    }

    public int getAchieveLessTen() {
        return achieveLessTen;
    }

    public int getAchieveEqualOne() {
        return achieveEqualOne;
    }
}
