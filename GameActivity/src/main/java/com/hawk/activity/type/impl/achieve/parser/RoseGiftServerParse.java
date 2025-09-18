package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.RoseGiftServerEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.game.protocol.Activity;

public class RoseGiftServerParse extends AchieveParser<RoseGiftServerEvent> {
    @Override
    public AchieveType geAchieveType() {
        return AchieveType.ROSE_GIFT_SERVER_NUM;
    }

    @Override
    public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
        return false;
    }

    @Override
    protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, RoseGiftServerEvent event) {
        if(event.getNum() >= achieveConfig.getConditionValue(0)){
            achieveData.setValue(0, event.getNum());
            return true;
        }
        return false;

    }
}
