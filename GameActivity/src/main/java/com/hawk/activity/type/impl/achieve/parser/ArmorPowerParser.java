package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.BattlePointChangeEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.helper.PlayerDataHelper;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.gamelib.player.PowerData;

public class ArmorPowerParser extends AchieveParser<BattlePointChangeEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.ARMOR_POWER;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		ActivityDataProxy dataGeter = PlayerDataHelper.getInstance().getDataGeter();
		PowerData data  = dataGeter.getPowerData(playerId);
		if(data == null){
			return false;
		}
		int armor = data.getSuperSoldierBattlePoint();
		int configValue = achieveConfig.getConditionValue(0);
		if (armor >= configValue) {
			armor = configValue;
		}
		achieveItem.setValue(0, armor);
		return true;
	}

	
	@Override
	protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig,
			BattlePointChangeEvent event) {
		int configValue = achieveConfig.getConditionValue(0);
		int value = event.getPowerData().getSuperSoldierBattlePoint();
		if (value >= configValue) {
			value = configValue;
		}
		achieveData.setValue(0, value);
		return true;
	}
}
