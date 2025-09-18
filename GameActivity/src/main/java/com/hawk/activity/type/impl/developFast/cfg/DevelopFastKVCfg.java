package com.hawk.activity.type.impl.developFast.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

/**
 * 实力飞升
 */
@HawkConfigManager.KVResource(file = "activity/develop_fast/develop_fast_cfg.xml")
public class DevelopFastKVCfg extends HawkConfigBase {
    //配置时间之后开服的服务器才能显示本活动
    private final String serverOpenTime;
    private final String serverEndOpenTime;
    //时间戳
    private long serverOpenTimeValue;

    private long serverEndOpenTimeValue;

    public DevelopFastKVCfg(){
        serverOpenTime = "";
        serverEndOpenTime = "";
    }

    //解析配置
    @Override
    protected boolean assemble() {
        //配置日期转换成时间错
        serverOpenTimeValue = HawkTime.parseTime(serverOpenTime);
        serverEndOpenTimeValue = HawkTime.parseTime(serverEndOpenTime);
        return true;
    }

    /**
     * 获得开服时间配置
     * @return 开服时间配置
     */
    public long getServerOpenTimeValue() {
        return serverOpenTimeValue;
    }

    public long getServerEndOpenTimeValue() {
        return serverEndOpenTimeValue;
    }
}
