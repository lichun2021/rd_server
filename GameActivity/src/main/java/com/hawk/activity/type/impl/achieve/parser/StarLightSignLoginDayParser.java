package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.StarLightSignLoginDayEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class StarLightSignLoginDayParser extends AchieveParser<StarLightSignLoginDayEvent> {
    @Override
    public AchieveType geAchieveType() {
        return AchieveType.STAR_LIGHT_SIGN_LOGIN_DAYS;
    }

    @Override
    public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
        return true;
    }

    @Override
    protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, StarLightSignLoginDayEvent event) {
        int configValue = achieveConfig.getConditionValue(0);
        int value = event.getLoginDays();
        if(achieveData.getValue(0) >= value){
            return false;
        }
        if (value > configValue) {
            value = configValue;
        }
        achieveData.setValue(0, value);
        return true;
    }
}
