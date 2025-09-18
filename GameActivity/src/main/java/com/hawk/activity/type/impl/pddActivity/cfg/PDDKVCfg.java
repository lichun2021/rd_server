package com.hawk.activity.type.impl.pddActivity.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRand;

@HawkConfigManager.KVResource(file = "activity/pdd/pdd_kv_cfg.xml")
public class PDDKVCfg extends HawkConfigBase {
    private final int activityId;
    /**
     * 起服延迟开放时间
     */
    private final int serverDelay;

    private final int successRate;

    private final int failNum;

    private final int shareCdTime;

    private final int shareDailyNum;

    private final int showName;

    private final String waterFloodWx;

    private final String waterFloodQq;

    private final int cannotPddTime;

    public PDDKVCfg(){
        activityId = 324;
        serverDelay = 0;
        successRate = 0;
        failNum = 0;
        shareCdTime = 0;
        shareDailyNum = 0;
        showName = 0;
        waterFloodWx = "";
        waterFloodQq = "";
        cannotPddTime = 0;
    }

    public long getServerDelay() {
        return serverDelay * 1000l;
    }

    public boolean isSuccess(){
        return HawkRand.randInt(10000) < successRate;
    }

    public int getFailNum() {
        return failNum;
    }

    public long getShareCdTime() {
        return shareCdTime * 1000l;
    }

    public int getShareDailyNum() {
        return shareDailyNum;
    }

    public boolean isShowName(){
        return showName == 1;
    }

    public String getWaterFloodWx() {
        return waterFloodWx;
    }

    public String getWaterFloodQq() {
        return waterFloodQq;
    }

    public long getCannotPddTime() {
        return cannotPddTime * 1000l;
    }
}
