package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.PDDGoldCostEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class PDDGoldCostParse extends AchieveParser<PDDGoldCostEvent> {
    @Override
    public AchieveType geAchieveType() {
        return AchieveType.PDD_GOLD_COST;
    }

    @Override
    public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
        return false;
    }

    @Override
    protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, PDDGoldCostEvent event) {
        int count = achieveData.getValue(0) + event.getNum();
        achieveData.setValue(0, count);
        return true;
    }
}
