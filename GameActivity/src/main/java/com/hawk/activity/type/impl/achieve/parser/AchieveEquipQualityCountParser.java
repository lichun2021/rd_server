package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.EquipQualityAchiveEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class AchieveEquipQualityCountParser extends AchieveParser<EquipQualityAchiveEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.ACHIVE_EQUIP_QUALIATY_COUNT;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	
	@Override
	protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig,
			EquipQualityAchiveEvent event) {
		
		int configQuality = achieveConfig.getConditionValue(0);
		int configValue = achieveConfig.getConditionValue(1);
		int addCount = event.equipQualityCount(configQuality);
		int value = achieveData.getValue(0);
		value += addCount;
		if(value >= configValue){
			value = configValue;
		}
		achieveData.setValue(0, value);
		return true;
	}
	
}
