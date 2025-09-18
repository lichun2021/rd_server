package com.hawk.activity.type.impl.order.task.impl;

import com.hawk.activity.event.impl.TWScoreEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;

public class TLWJoinParser implements OrderTaskParser<TWScoreEvent> {
    @Override
    public OrderTaskType getTaskType() {
        return OrderTaskType.TLW_JOIN;
    }

    @Override
    public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, TWScoreEvent event) {
        if(!event.isLeagua()){
            return false;
        }
        if(event.getEnterTime()<=0){
            return false;
        }
        return onAddValue(dataEntity, cfg, orderItem, 1);
    }
}
