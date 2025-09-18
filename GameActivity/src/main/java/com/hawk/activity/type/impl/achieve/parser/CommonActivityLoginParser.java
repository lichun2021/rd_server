package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.CommonActivityLoginEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import org.hawk.os.HawkTime;

public class CommonActivityLoginParser extends AchieveParser<CommonActivityLoginEvent> {
    @Override
    public AchieveType geAchieveType() {
        return AchieveType.COMMON_ACTIVITY_LOGIN;
    }

    @Override
    public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
        return true;
    }

    @Override
    protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, CommonActivityLoginEvent event) {
        int activityId = achieveConfig.getConditionValue(0);
        if(activityId != event.getActivityId()){
            return false;
        }
        int lastTime = achieveData.getValue(1);
        long now = HawkTime.getMillisecond();
        if(HawkTime.isSameDay(now, lastTime * 1000l)){
            return false;
        }
        achieveData.setValue(1, (int)(now / 1000l));
        int value = achieveData.getValue(0);
        value = value + 1;
        int configValue = achieveConfig.getConditionValue(1);
        if (value > configValue) {
            value = configValue;
        }
        achieveData.setValue(0, value);
        return true;
    }
}
