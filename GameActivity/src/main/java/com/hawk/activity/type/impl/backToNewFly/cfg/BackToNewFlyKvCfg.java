package com.hawk.activity.type.impl.backToNewFly.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

/**
* 本文件自动生成，会被覆盖，不要手改非自动生成部分
*/
@HawkConfigManager.KVResource(file = "activity/back_to_new_fly/back_to_new_fly_kv.xml")
public class BackToNewFlyKvCfg extends HawkConfigBase{
    private final int oldDuration;
    private final int rewardMail;
    private final int duration;
    private final int serverDelay;
    private final String rewardItemGetNew;

    public BackToNewFlyKvCfg(){
        this.oldDuration = 0;
        this.rewardMail = 0;
        this.duration = 0;
        this.serverDelay = 0;
        this.rewardItemGetNew = "";
    }

    public long getOldDuration(){
        return this.oldDuration * 1000l;
    }

    public int getRewardMail(){
        return this.rewardMail;
    }

    public long getDuration(){
        return this.duration * 1000l;
    }

    public int getServerDelay(){
        return this.serverDelay;
    }

    public String getRewardItemGetNew(){
        return this.rewardItemGetNew;
    }

    @Override
    protected boolean assemble() {
        try {
            return true;
        } catch (Exception e) {
            HawkException.catchException(e);
            return false;
        }
    }

}