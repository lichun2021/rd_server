package com.hawk.activity.type.impl.achieve.parser;

import org.hawk.log.HawkLog;

import com.hawk.activity.event.impl.BattlePointChangeEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.gamelib.player.PowerChangeData;
import com.hawk.gamelib.player.PowerData;
import com.hawk.log.LogConst.PowerChangeReason;

public class PowerUpSoldierParser extends AchieveParser<BattlePointChangeEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.POWER_UP_SOLDIER;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, BattlePointChangeEvent event) {

		PowerChangeReason reason = event.getReason();
		if (event.isSoliderCure() || reason == PowerChangeReason.LMJY_INIT || reason == PowerChangeReason.NATION_HOSPITAL_COLLECT) {
			return false;
		}

		PowerChangeData changeData = event.getChangeData();
		PowerData powerData = event.getPowerData();
		// 如果玩家当前建筑战力,等于此次建筑变化战力,则此次战力变化事件不进行处理
		if (powerData.getBuildBattlePoint() == changeData.getBuildBattleChange()) {
			HawkLog.logPrintln("PowerUpSoldierParser discard powerChange, playerId: {}, changeData: {}", event.getPlayerId(), changeData);
			return false;
		}
		int armyAdd = changeData.getArmyBattleChange();
		// 除了初始化/奖励/训练/晋升士兵外,其他来源的士兵战力变化不计入分数
		if (armyAdd > 0 && (reason == PowerChangeReason.INIT_SOLDIER || reason == PowerChangeReason.AWARD_SOLDIER || reason == PowerChangeReason.TRAIN_SOLDIER)) {
			int val = achieveData.getValue(0) + armyAdd;
			int conditionVal = achieveConfig.getConditionValue(0);
			if (val > conditionVal) {
				val = conditionVal;
			}
			achieveData.setValue(0, val);
		}
		return true;
	}

}
