package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.UseItemSpeedUpQueueEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class UseItemSpeedUpQueueParser extends AchieveParser<UseItemSpeedUpQueueEvent> {
    @Override
    public AchieveType geAchieveType() {
        return AchieveType.USE_ITEM_SPEED_UP_QUEUE;
    }

    @Override
    public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
        return true;
    }

    @Override
    protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, UseItemSpeedUpQueueEvent event) {
        int type = achieveConfig.getConditionValue(1);
        if(event.getQueueType() != type){
            return false;
        }
        int configValue = achieveConfig.getConditionValue(0);

        //直接计数
        int totalVal = achieveData.getValue(0) + event.getMinute();
        if (totalVal > configValue) {
            totalVal = configValue;
        }
        achieveData.setValue(0, totalVal);
        return true;
    }
}
