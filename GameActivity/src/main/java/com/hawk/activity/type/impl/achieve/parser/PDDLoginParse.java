package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.PDDLoginEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class PDDLoginParse  extends AchieveParser<PDDLoginEvent> {
    @Override
    public AchieveType geAchieveType() {
        return AchieveType.PDD_LOGIN;
    }

    @Override
    public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
        return false;
    }

    @Override
    protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, PDDLoginEvent event) {
        int count = achieveData.getValue(0) + 1;
        achieveData.setValue(0, count);
        return true;
    }
}
