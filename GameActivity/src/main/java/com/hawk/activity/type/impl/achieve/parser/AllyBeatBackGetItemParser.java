package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.AllyBeatBackGetItemEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class AllyBeatBackGetItemParser extends AchieveParser<AllyBeatBackGetItemEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.ALLY_BEAT_BACK_GET_ITEM;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {	
		return true;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, AllyBeatBackGetItemEvent event) {
		int itemId = achieveConfig.getConditionValue(0);
		int num = achieveData.getValue(0);
		Integer getNum = event.getItemCount().get(itemId);
		if (getNum == null) {
			return false;
		}
		int newNum = num + getNum;
		newNum = newNum > achieveConfig.getConditionValue(1) ? achieveConfig.getConditionValue(1) : newNum;
		achieveData.setValue(0, newNum);
		
		return true;
	}

}
