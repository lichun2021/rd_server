package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.ArmourStrengthenEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class ArmourStrengthenParser extends AchieveParser<ArmourStrengthenEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.ARMOUR_STRENGTHEN_TIMES;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return true;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, ArmourStrengthenEvent event) {
		//只论次数不讲等级
		int configValue = achieveConfig.getConditionValue(0);
		int srcVal = achieveItem.getValue(0);
		int newVal = srcVal + 1; 
		if(srcVal >= configValue){
			return false;
		}
		achieveItem.setValue(0, newVal);
		return true;
	}
}
