package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.BattlePointChangeEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.helper.PlayerDataHelper;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.gamelib.player.PowerData;

public class TechPowerParser extends AchieveParser<BattlePointChangeEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.TECH_POWER;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		ActivityDataProxy dataGeter = PlayerDataHelper.getInstance().getDataGeter();
		PowerData data = dataGeter.getPowerData(playerId);
		if(data == null){
			return false;
		}
		int tech = data.getTechBattlePoint() + data.getPlantScienceBattlePoint();
		int configValue = achieveConfig.getConditionValue(0);
		if (tech >= configValue) {
			tech = configValue;
		}
		achieveItem.setValue(0, tech);
		return true;
	}

	
	@Override
	protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig,
			BattlePointChangeEvent event) {
		int configValue = achieveConfig.getConditionValue(0);
		int value = event.getPowerData().getTechBattlePoint();
		if (value >= configValue) {
			value = configValue;
		}
		achieveData.setValue(0, value);
		return true;
	}
}
