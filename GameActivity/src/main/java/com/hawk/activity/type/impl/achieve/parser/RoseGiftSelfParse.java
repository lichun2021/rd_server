package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.RoseGiftSelfEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class RoseGiftSelfParse extends AchieveParser<RoseGiftSelfEvent> {
    @Override
    public AchieveType geAchieveType() {
        return AchieveType.ROSE_GIFT_SELF_NUM;
    }

    @Override
    public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
        return false;
    }

    @Override
    protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, RoseGiftSelfEvent event) {
        if(event.getNum() <= achieveData.getValue(0)){
            return false;
        }
        achieveData.setValue(0, event.getNum());
        return true;
    }
}
