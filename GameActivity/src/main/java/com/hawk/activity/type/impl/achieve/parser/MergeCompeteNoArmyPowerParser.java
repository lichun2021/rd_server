package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.BattlePointChangeEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.helper.PlayerDataHelper;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.gamelib.player.PowerData;

public class MergeCompeteNoArmyPowerParser extends AchieveParser<BattlePointChangeEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.MERGE_COMPETE_NOARMY_POWER;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		ActivityDataProxy dataGeter = PlayerDataHelper.getInstance().getDataGeter();
		PowerData data  = dataGeter.getPowerData(playerId);
		if(data == null){
			return false;
		}
		long noarmypower = data.getBattlePoint() - data.getArmyBattlePoint() - data.getTrapBattlePoint();
		int configValue = achieveConfig.getConditionValue(0);
		if (noarmypower >= configValue) {
			noarmypower = configValue;
		}
		achieveItem.setValue(0, (int)noarmypower);
		return true;
	}

	
	@Override
	protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, BattlePointChangeEvent event) {
		int configValue = achieveConfig.getConditionValue(0);
		PowerData powerData = event.getPowerData();
		long noarmypower = Math.max(powerData.getBattlePoint() - powerData.getArmyBattlePoint() - powerData.getTrapBattlePoint(), 0);
		if (noarmypower >= configValue) {
			noarmypower = configValue;
		}
		achieveData.setValue(0, (int)noarmypower);
		return true;
	}
	
}
