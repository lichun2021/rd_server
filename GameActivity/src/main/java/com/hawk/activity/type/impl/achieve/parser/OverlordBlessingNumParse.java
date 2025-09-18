package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.OverlordBlessingEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class OverlordBlessingNumParse extends AchieveParser<OverlordBlessingEvent> {
    @Override
    public AchieveType geAchieveType() {
        return AchieveType.OVERLORD_BLESSING_NUM;
    }

    @Override
    public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
        return false;
    }

    @Override
    protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, OverlordBlessingEvent event) {
        if(event.getNum() >= achieveConfig.getConditionValue(0)){
            achieveData.setValue(0, event.getNum());
            return true;
        }
        return false;

    }
}
