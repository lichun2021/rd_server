package com.hawk.activity.type.impl.achieve.parser;

import java.util.List;

import com.hawk.activity.event.impl.RandomHeroEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.datatype.ListValueData;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class GachaParser extends AchieveParser<RandomHeroEvent> {
	
	private ListValueData listValueData = new ListValueData();
	
	@Override
	public AchieveType geAchieveType() {
		return AchieveType.GACHA;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, RandomHeroEvent event) {
		List<Integer> conditionValues = achieveConfig.getConditionValues();
		if (listValueData.isInList(conditionValues, event.getGachaType()) == false) {
			return false;
		}
		int count = achieveItem.getValue(0) + event.getCount();
		achieveItem.setValue(0, count);
		return true;
	}
}
