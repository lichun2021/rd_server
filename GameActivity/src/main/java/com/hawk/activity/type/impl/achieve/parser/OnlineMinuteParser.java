package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.OnlineMinuteEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * 在线时间
 */
public class OnlineMinuteParser extends AchieveParser<OnlineMinuteEvent> {
    @Override
    public AchieveType geAchieveType() {
        return AchieveType.ONLINE_MINUTES;
    }

    @Override
    public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
        return true;
    }

    @Override
    protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, OnlineMinuteEvent event) {
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
