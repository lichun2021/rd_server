package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.HomeRoundItemCostEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class HomeRoundItemCostTimesParser extends AchieveParser<HomeRoundItemCostEvent> {

    @Override
    public AchieveType geAchieveType() {
        return AchieveType.HOME_ROUND_ITEM_COST;
    }

    @Override
    public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
        return true;
    }

    @Override
    protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, HomeRoundItemCostEvent event) {
        int configId = achieveConfig.getConditionValue(0);
        if (configId != event.getItemId()) {
            return false;
        }
        int configValue = achieveConfig.getConditionValue(1);
        int value = achieveData.getValue(0) + event.getNum();
        if (value > configValue) {
            value = configValue;
        }
        achieveData.setValue(0, value);
        return true;
    }
}
