package com.hawk.activity.type.impl.diffNewServerTech.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

@HawkConfigManager.KVResource(file = "activity/service_bonus/%s/bonus_cfg.xml", autoLoad=false, loadParams="323")
public class DiffNewServerTechKVCfg extends HawkConfigBase {
    /**
     * 起服延迟开放时间
     */
    private final int serverDelay;
    private final String time;

    private long timeValue;

    public DiffNewServerTechKVCfg(){
        serverDelay = 0;
        time = "";
    }

    @Override
    protected boolean assemble() {
        timeValue = HawkTime.parseTime(time);
        return true;
    }

    public long getServerDelay() {
        return serverDelay * 1000l;
    }

    public long getTimeValue() {
        return timeValue;
    }
}
