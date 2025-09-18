package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.SupplyCrateGuildRechargeEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class SupplyCrateGuildRechargeParser  extends AchieveParser<SupplyCrateGuildRechargeEvent> {
    @Override
    public AchieveType geAchieveType() {
        return AchieveType.SUPPLY_CRATE_GUILD_RECHARGE;
    }

    @Override
    public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
        return true;
    }

    @Override
    protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, SupplyCrateGuildRechargeEvent event) {
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
