package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.GivenItemCostEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class GivenItemCostParser extends AchieveParser<GivenItemCostEvent> {

    @Override
    public AchieveType geAchieveType() {
        return AchieveType.GIVEN_ITEM_COST_21070054;
    }

    @Override
    public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
        return false;
    }

    @Override
    protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, GivenItemCostEvent event) {
        if(21070054 != event.getItemId()){
            return false;
        }
        int configValue = achieveConfig.getConditionValue(0);
        int value = achieveData.getValue(0) + event.getNum();
        if (value > configValue) {
            value = configValue;
        }
        achieveData.setValue(0, value);
        return true;
    }
}
