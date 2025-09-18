package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.DYZZWinWithBaseEqualOneEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class DYZZWinWithBaseEqualOneParser extends AchieveParser<DYZZWinWithBaseEqualOneEvent> {
    @Override
    public AchieveType geAchieveType() {
        return AchieveType.DYZZ_WIN_WITH_BASE_EQUAL_ONE;
    }

    @Override
    public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
        return false;
    }

    @Override
    protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, DYZZWinWithBaseEqualOneEvent event) {
        int count = achieveData.getValue(0) + 1;
        int configValue = achieveConfig.getConditionValue(0);
        if(count > configValue){
            achieveData.setValue(0, configValue);
        }else {
            achieveData.setValue(0, count);
        }
        return true;
    }
}
