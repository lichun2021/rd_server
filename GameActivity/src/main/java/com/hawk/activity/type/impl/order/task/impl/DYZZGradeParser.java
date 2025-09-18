package com.hawk.activity.type.impl.order.task.impl;

import com.hawk.activity.event.impl.DYZZGradeEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;
import com.hawk.activity.type.impl.seasonActivity.cfg.SeasonOpenTimeCfg;
import com.hawk.game.protocol.Activity;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

public class DYZZGradeParser implements OrderTaskParser<DYZZGradeEvent> {
    @Override
    public OrderTaskType getTaskType() {
        return OrderTaskType.DYZZ_GRADE;
    }

    @Override
    public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, DYZZGradeEvent event) {
        long now = HawkTime.getMillisecond();
        SeasonOpenTimeCfg timeCfg = null;
        for(SeasonOpenTimeCfg openTimeCfg : HawkConfigManager.getInstance().getConfigIterator(SeasonOpenTimeCfg.class)){
            if(now < openTimeCfg.getShowTimeValue() || now > openTimeCfg.getHiddenTimeValue()){
                continue;
            }
            timeCfg = openTimeCfg;
        }
        if(timeCfg == null){
            return false;
        }
        int termId = timeCfg.getMatchTermId(Activity.SeasonMatchType.S_DYZZ_VALUE);
        if(termId == 0){
            return false;
        }
        if(event.getSeasonTerm()!=termId){
            return false;
        }
        if (event.getGrade() == cfg.getConditionList().get(0)) {
            return onAddValue(dataEntity, cfg, orderItem, 1);
        }
        if(event.getGrade() == 6 && cfg.getConditionList().get(0) == 5){
            return onAddValue(dataEntity, cfg, orderItem, 1);
        }
        return false;
    }
}
