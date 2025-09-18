package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.RechargeAllRmbEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class RechargeAllRmbParse extends AchieveParser<RechargeAllRmbEvent> {
    @Override
    public AchieveType geAchieveType() {
        return AchieveType.RECHARGE_ALL_RMB;
    }

    @Override
    public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
        return false;
    }

    @Override
    protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, RechargeAllRmbEvent event) {
        int count = achieveData.getValue(0) + event.getRmb();
        int configValue = achieveConfig.getConditionValue(0);
        if(count > configValue){
            achieveData.setValue(0, configValue);
        }else {
            achieveData.setValue(0, count);
        }
        return true;
    }
}
