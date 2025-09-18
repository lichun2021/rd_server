package com.hawk.activity.type.impl.developFastOld.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

/**
 * 实力飞升
 */
@HawkConfigManager.KVResource(file = "activity/develop_fast_old/develop_fast_old_cfg.xml")
public class DevelopFastOldKVCfg extends HawkConfigBase {
    //配置时间之后开服的服务器才能显示本活动
    private final String serverOpenTime;
    //时间戳
    private long serverOpenTimeValue;

    public DevelopFastOldKVCfg(){
        serverOpenTime = "";
    }

    //解析配置
    @Override
    protected boolean assemble() {
        //配置日期转换成时间错
        serverOpenTimeValue = HawkTime.parseTime(serverOpenTime);
        return true;
    }

    /**
     * 获得开服时间配置
     * @return 开服时间配置
     */
    public long getServerOpenTimeValue() {
        return serverOpenTimeValue;
    }
}
