package com.hawk.activity.type.impl.exchangeTip;

import org.hawk.config.HawkConfigBase;

/**
 * 兑换提醒配置需要继承此类
 */
public abstract class AExchangeTipConfig extends HawkConfigBase {
    /**
     * 获得配置id
     * @return 配置id
     */
    public abstract int getId();
}
