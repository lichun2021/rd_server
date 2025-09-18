package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.SpaceMachinePointAddEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class SpaceMachinePointParser extends AchieveParser<SpaceMachinePointAddEvent> {
    @Override
    public AchieveType geAchieveType() {
        return AchieveType.SPACE_MACHINE_POINT;
    }

    @Override
    public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
        return false;
    }

    @Override
    protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, SpaceMachinePointAddEvent event) {
        int count = achieveData.getValue(0) + event.getAddPoint();
        int configValue = achieveConfig.getConditionValue(0);
        count = Math.min(configValue, count);
        achieveData.setValue(0, count);
        return true;
    }
    
}
