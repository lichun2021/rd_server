package com.hawk.activity.type.impl.exchangeTip;

import java.util.Set;

/**
 * 兑换提醒前端需要实现此接口
 */
public interface IExchangeTipEntity {
    Set<Integer> getTipSet();

    void setTipSet(Set<Integer> tips);
}
