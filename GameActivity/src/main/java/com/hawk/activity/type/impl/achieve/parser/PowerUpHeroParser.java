package com.hawk.activity.type.impl.achieve.parser;

import org.hawk.log.HawkLog;

import com.hawk.activity.event.impl.BattlePointChangeEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.gamelib.player.PowerChangeData;
import com.hawk.gamelib.player.PowerData;

public class PowerUpHeroParser extends AchieveParser<BattlePointChangeEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.POWER_UP_HERO;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, BattlePointChangeEvent event) {
		PowerChangeData changeData = event.getChangeData();
		PowerData powerData = event.getPowerData();
		// 如果玩家当前建筑战力,等于此次建筑变化战力,则此次战力变化事件不进行处理
		if(powerData.getBuildBattlePoint() == changeData.getBuildBattleChange()){
			HawkLog.logPrintln("PowerUpHeroParser discard powerChange, playerId: {}, changeData: {}", event.getPlayerId(), changeData);
			return false;
		}
		if (changeData.getHeroBattleChange() > 0) {
			int val = achieveData.getValue(0) + changeData.getHeroBattleChange();
			int conditionVal = achieveConfig.getConditionValue(0);
			if (val > conditionVal) {
				val = conditionVal;
			}
			achieveData.setValue(0, val);
		}
		return true;
	}

}
