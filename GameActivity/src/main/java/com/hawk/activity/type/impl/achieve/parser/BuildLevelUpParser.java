package com.hawk.activity.type.impl.achieve.parser;

import java.util.List;

import com.hawk.activity.event.impl.BuildingLevelUpEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.helper.PlayerDataHelper;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.datatype.ListValueData;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class BuildLevelUpParser extends AchieveParser<BuildingLevelUpEvent> {

	private ListValueData listValueData = new ListValueData();
	
	@Override
	public AchieveType geAchieveType() {
		return AchieveType.BUILD_LEVEL_UP;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		ActivityDataProxy dataGeter = PlayerDataHelper.getInstance().getDataGeter();
		List<Integer> conditionValues = achieveConfig.getConditionValues();
		int buildLevel = 0;
		for (int i = 0; i < conditionValues.size() - 1; i++) {
			int maxLevel = dataGeter.getBuildMaxLevel(playerId, conditionValues.get(i));
			if (maxLevel > buildLevel) {
				buildLevel = maxLevel;
			}
		}
		
		int configLevel = achieveConfig.getConditionValue(conditionValues.size() - 1);
		if (buildLevel > configLevel) {
			buildLevel = configLevel;
		}
		
		achieveItem.setValue(0, buildLevel);
		return true;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, BuildingLevelUpEvent event) {
		List<Integer> conditionValues = achieveConfig.getConditionValues();
		if (listValueData.isInList(conditionValues, event.getBuildType()) == false) {
			return false;
		}

		int oldLevel = achieveItem.getValue(0);
		if (oldLevel >= event.getLevel()) {
			return false;
		}
		achieveItem.setValue(0, event.getLevel());
		return true;
	}

}
