package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.DYZZWinEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.game.protocol.Activity;

public class DYZZWinParser extends AchieveParser<DYZZWinEvent> {
    @Override
    public AchieveType geAchieveType() {
        return AchieveType.DYZZ_WIN;
    }

    @Override
    public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
        return false;
    }

    @Override
    protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, DYZZWinEvent event) {
        if(achieveData.getState() != Activity.AchieveState.NOT_ACHIEVE_VALUE){
            return false;
        }
        if(event.isWin()){
            int count = achieveData.getValue(0) + 1;
            int configValue = achieveConfig.getConditionValue(0);
            if(count > configValue){
                achieveData.setValue(0, configValue);
            }else {
                achieveData.setValue(0, count);
            }
        }else {
            achieveData.setValue(0, 0);
        }
        return true;
    }
}
