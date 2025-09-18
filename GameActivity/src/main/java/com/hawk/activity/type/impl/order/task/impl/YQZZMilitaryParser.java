package com.hawk.activity.type.impl.order.task.impl;

import com.hawk.activity.event.impl.YQZZScoreEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;


public class YQZZMilitaryParser implements OrderTaskParser<YQZZScoreEvent> {

    @Override
    public OrderTaskType getTaskType() {
        return OrderTaskType.YQZZ_MILITARY;
    }

    @Override
    public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, YQZZScoreEvent event) {
        if (event.getMilitray() >= cfg.getConditionList().get(0)) {
            return onAddValue(dataEntity, cfg, orderItem, 1);
        }
        return false;
    }
}
