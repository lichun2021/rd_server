package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.EnergyCountEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.helper.PlayerDataHelper;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class EnergyCountParser extends AchieveParser<EnergyCountEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.ENERGY_COUNT;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		ActivityDataProxy dataGeter = PlayerDataHelper.getInstance().getDataGeter();
		int enerygy = dataGeter.getEnergyCount(playerId);
		int configValue = achieveConfig.getConditionValue(0);
		if (enerygy >= configValue) {
			enerygy = configValue;
		}
		achieveItem.setValue(0, enerygy);
		return true;
	}

	
	@Override
	protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig,
			EnergyCountEvent event) {
		int configValue = achieveConfig.getConditionValue(0);
		int value = event.getEnergy();
		if (value >= configValue) {
			value = configValue;
		}
		achieveData.setValue(0, value);
		return true;
	}
}
