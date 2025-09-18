package com.hawk.activity.type.impl.urlReward;

public class URLServerOpenCfg extends URLRewardBaseCfg {
    private final String serverOpenTime;
    private final String serverEndTime;

    public URLServerOpenCfg() {
        serverOpenTime = "0";
        serverEndTime = "0";
    }

    public String getServerOpenTime() {
        return serverOpenTime;
    }

    public String getServerEndTime() {
        return serverEndTime;
    }
}
