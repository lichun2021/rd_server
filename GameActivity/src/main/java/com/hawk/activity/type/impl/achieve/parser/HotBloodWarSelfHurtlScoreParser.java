package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.HotBloodWarScoreEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class HotBloodWarSelfHurtlScoreParser extends AchieveParser<HotBloodWarScoreEvent> {
    @Override
    public AchieveType geAchieveType() {
        return AchieveType.HOT_BLOOD_WAR_SOLDIER_LOSS_SCORE;
    }

    @Override
    public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
        return false;
    }
 
    @Override
    protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, HotBloodWarScoreEvent event) {
    	if(event.getSelfHurtScore() <= 0){
    		return false;
    	}
    	int configValue = achieveConfig.getConditionValue(0);
		int value = (int) event.getSelfHurtScore();
		if (value > configValue) {
			value = configValue;
		}
		achieveData.setValue(0, value);
		return true;
    }
}
