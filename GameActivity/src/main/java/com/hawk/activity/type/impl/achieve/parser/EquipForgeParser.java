package com.hawk.activity.type.impl.achieve.parser;

import java.util.List;

import com.hawk.activity.event.impl.EquipForgeEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class EquipForgeParser extends AchieveParser<EquipForgeEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.FORGE_EQUIP_COUNT;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, EquipForgeEvent event) {
		List<Integer> conditionValues = achieveConfig.getConditionValues();
		int qualityLimit = conditionValues.get(0);
		int levelLimit = conditionValues.get(1);
		if (event.getQuality() >= qualityLimit && event.getLvl() >= levelLimit) {
			achieveItem.setValue(0, achieveItem.getValue(0) + 1);
		}
		return true;
	}
}
