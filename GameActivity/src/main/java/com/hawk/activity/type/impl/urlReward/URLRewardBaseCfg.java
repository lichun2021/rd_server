package com.hawk.activity.type.impl.urlReward;

import org.hawk.config.HawkConfigBase;

public class URLRewardBaseCfg extends HawkConfigBase {
    public final String urlDailyReward;

    public URLRewardBaseCfg(){
        urlDailyReward = "";
    }

    public String getUrlDailyReward() {
        return urlDailyReward;
    }
}
