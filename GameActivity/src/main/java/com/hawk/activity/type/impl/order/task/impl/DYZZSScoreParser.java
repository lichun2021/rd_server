package com.hawk.activity.type.impl.order.task.impl;

import com.hawk.activity.event.impl.DYZZScoreEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;

public class DYZZSScoreParser implements OrderTaskParser<DYZZScoreEvent> {
    @Override
    public OrderTaskType getTaskType() {
        return OrderTaskType.DYZZS_SCORE;
    }

    @Override
    public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, DYZZScoreEvent event) {
        if(!event.isLeagua()){
            return false;
        }
        if (event.getScore() >= cfg.getConditionList().get(0)) {
            return onAddValue(dataEntity, cfg, orderItem, 1);
        }
        return false;
    }
}
