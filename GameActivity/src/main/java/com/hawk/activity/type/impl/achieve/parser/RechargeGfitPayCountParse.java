package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.PayGiftBuyEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class RechargeGfitPayCountParse extends AchieveParser<PayGiftBuyEvent> {
    @Override
    public AchieveType geAchieveType() {
        return AchieveType.RECHARGE_GIFT_PAY_COUNT;
    }

    @Override
    public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
        return false;
    }

    @Override
    protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, PayGiftBuyEvent event) {
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
