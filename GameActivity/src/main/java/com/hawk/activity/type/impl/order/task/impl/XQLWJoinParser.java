package com.hawk.activity.type.impl.order.task.impl;

import com.hawk.activity.event.impl.XQScoreEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;

public class XQLWJoinParser implements OrderTaskParser<XQScoreEvent> {
    @Override
    public OrderTaskType getTaskType() {
        return OrderTaskType.XQLW_JOIN;
    }

    @Override
    public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, XQScoreEvent event) {
        if(!event.isLeagua()){
            return false;
        }
        return onAddValue(dataEntity, cfg, orderItem, 1);
    }
}
