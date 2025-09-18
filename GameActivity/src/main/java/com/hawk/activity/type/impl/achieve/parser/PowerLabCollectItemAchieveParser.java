package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.PowerLabItemDropAchieveEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class PowerLabCollectItemAchieveParser extends AchieveParser<PowerLabItemDropAchieveEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.POWER_LAB_COLLECT_ACHIEVE;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig,
			PowerLabItemDropAchieveEvent event) {
		int itemId = achieveConfig.getConditionValue(0);
		if(event.getItemId() != itemId){
			return false;
		}
		int num = achieveData.getValue(0);
		Integer getNum = event.getCount();
		int newNum = num + getNum;
		newNum = newNum > achieveConfig.getConditionValue(1) ? achieveConfig.getConditionValue(1) : newNum;
		achieveData.setValue(0, newNum);
		return true;
	}

}
