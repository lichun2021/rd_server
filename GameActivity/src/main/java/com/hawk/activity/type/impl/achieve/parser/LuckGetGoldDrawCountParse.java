package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.LuckGetGoldDrawEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class LuckGetGoldDrawCountParse extends AchieveParser<LuckGetGoldDrawEvent> {
    @Override
    public AchieveType geAchieveType() {
        return AchieveType.LUCK_GET_GOLD_DRAW;
    }

    @Override
    public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
        return false;
    }

    @Override
    protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, LuckGetGoldDrawEvent event) {
        int dataValue = achieveData.getValue(0);
        if(event.getNum() < dataValue){
            return false;
        }
        int configValue = achieveConfig.getConditionValue(0);
        if(event.getNum() > configValue){
            achieveData.setValue(0, configValue);
        }else {
            achieveData.setValue(0, event.getNum());
        }
        return true;
    }
}
