package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.PlantSoldierFactoryDrawEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class PlantSoldierFactoryDrawParser extends AchieveParser<PlantSoldierFactoryDrawEvent> {
    @Override
    public AchieveType geAchieveType() {
        return AchieveType.PLANT_SOLDIER_FACTORY_DRAW;
    }

    @Override
    public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
        return false;
    }

    @Override
    protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, PlantSoldierFactoryDrawEvent event) {
        int configValue = achieveConfig.getConditionValue(0);
        int value = event.getDrawConsume();
        if (value > configValue) {
            value = configValue;
        }
        achieveData.setValue(0, value);
        return true;
    }
}
