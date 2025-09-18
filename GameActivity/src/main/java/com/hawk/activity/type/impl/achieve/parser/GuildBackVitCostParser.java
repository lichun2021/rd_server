package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.GuildBackVitCostEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class GuildBackVitCostParser extends AchieveParser<GuildBackVitCostEvent> {
    @Override
    public AchieveType geAchieveType() {
        return AchieveType.GUILD_BACK_VIT_COST;
    }

    @Override
    public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
        return true;
    }

    @Override
    protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, GuildBackVitCostEvent event) {
        int configValue = achieveConfig.getConditionValue(0);
        int value = event.getNum();
        if(achieveData.getValue(0) >= value){
            return false;
        }
        if (value > configValue) {
            value = configValue;
        }
        achieveData.setValue(0, value);
        return true;
    }
}
