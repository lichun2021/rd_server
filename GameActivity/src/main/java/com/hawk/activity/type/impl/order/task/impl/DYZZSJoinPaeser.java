package com.hawk.activity.type.impl.order.task.impl;

import com.hawk.activity.event.impl.DYZZScoreEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;

public class DYZZSJoinPaeser implements OrderTaskParser<DYZZScoreEvent> {
    @Override
    public OrderTaskType getTaskType() {
        return OrderTaskType.DYZZS_JOIN;
    }

    @Override
    public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, DYZZScoreEvent event) {
        if(!event.isLeagua()){
            return false;
        }
        if(event.getEnterTime()<=0){
            return false;
        }
        return onAddValue(dataEntity, cfg, orderItem, 1);
    }
}
