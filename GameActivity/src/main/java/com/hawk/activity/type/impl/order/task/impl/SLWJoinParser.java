package com.hawk.activity.type.impl.order.task.impl;

import com.hawk.activity.event.impl.SWScoreEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;

public class SLWJoinParser implements OrderTaskParser<SWScoreEvent> {
    @Override
    public OrderTaskType getTaskType() {
        return OrderTaskType.SLW_JOIN;
    }

    @Override
    public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, SWScoreEvent event) {
        if(!event.isLeagua()){
            return false;
        }
        if(event.getEnterTime()<=0){
            return false;
        }
        return onAddValue(dataEntity, cfg, orderItem, 1);
    }
}
