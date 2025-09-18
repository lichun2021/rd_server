package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.BattlePointChangeEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.helper.PlayerDataHelper;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.gamelib.player.PowerData;

public class BattlePointParser extends AchieveParser<BattlePointChangeEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.BATTLE_POINT;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		ActivityDataProxy dataGeter = PlayerDataHelper.getInstance().getDataGeter();
		PowerData data = dataGeter.getPowerData(playerId);
		if(data == null){
			return false;
		}
		long battlePoint = data.getBattlePoint();
		int configBattlePoint = achieveConfig.getConditionValue(0);
		if (battlePoint > configBattlePoint) {
			battlePoint = configBattlePoint;
		}
		achieveItem.setValue(0, (int)Math.min(Integer.MAX_VALUE - 1, battlePoint));
		return true;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, BattlePointChangeEvent event) {
		int configValue = achieveConfig.getConditionValue(0);
		long value = event.getPowerData().getBattlePoint();
		if (value > configValue) {
			value = configValue;
		}
		achieveItem.setValue(0, (int)Math.min(Integer.MAX_VALUE - 1, value));
		return true;
	}
}
