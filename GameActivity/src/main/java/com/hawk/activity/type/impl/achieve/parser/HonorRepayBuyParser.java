package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.HonorRepayBuyEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * 荣耀返利次数
 */
public class HonorRepayBuyParser extends AchieveParser<HonorRepayBuyEvent> {

    @Override
    public AchieveType geAchieveType() {
        return AchieveType.HONOR_REPAY_BUY;
    }

    @Override
    public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
        return false;
    }

    @Override
    protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, HonorRepayBuyEvent event) {
        int configValue = achieveConfig.getConditionValue(0);
        int value = achieveItem.getValue(0) + event.getTimes();
        if (value > configValue) {
            value = configValue;
        }
        achieveItem.setValue(0, value);
        return true;
    }
}