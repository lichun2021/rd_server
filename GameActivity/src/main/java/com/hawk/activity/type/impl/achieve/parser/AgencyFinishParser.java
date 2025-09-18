package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.AgencyFinishEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class AgencyFinishParser extends AchieveParser<AgencyFinishEvent> {
    @Override
    public AchieveType geAchieveType() {
        return AchieveType.AGENCY_FINISH;
    }

    @Override
    public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
        return true;
    }

    @Override
    protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, AgencyFinishEvent event) {
        int configValue = achieveConfig.getConditionValue(0);
        //直接计数
        int totalVal = achieveData.getValue(0) + 1;
        if (totalVal > configValue) {
            totalVal = configValue;
        }
        achieveData.setValue(0, totalVal);
        return true;
    }
}
