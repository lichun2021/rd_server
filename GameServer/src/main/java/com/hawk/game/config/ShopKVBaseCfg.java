package com.hawk.game.config;


import org.hawk.config.HawkConfigBase;
import org.hawk.os.HawkTime;

import java.util.concurrent.TimeUnit;

public class ShopKVBaseCfg extends HawkConfigBase {
    public final int shopId;
    public final String startTime;
    public final int refreshTime;

    /** 战斗日零点时间戳*/
    private long startTimeValue;

    public ShopKVBaseCfg(){
        this.shopId = 0;
        this.startTime = "";
        this.refreshTime = 0;
    }

    @Override
    protected boolean assemble() {
        this.startTimeValue = HawkTime.parseTime(startTime);
        return true;
    }

    public int getShopId() {
        return shopId;
    }

    public String getStartTime() {
        return startTime;
    }

    public long getRefreshTime() {
        return TimeUnit.DAYS.toMillis(refreshTime);
    }

    public long getStartTimeValue() {
        return startTimeValue;
    }
}
