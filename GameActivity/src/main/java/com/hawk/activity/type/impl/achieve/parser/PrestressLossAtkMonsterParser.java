package com.hawk.activity.type.impl.achieve.parser;

import java.util.List;

import org.hawk.log.HawkLog;

import com.hawk.activity.event.impl.MonsterAttackEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.game.protocol.World.MonsterType;

public class PrestressLossAtkMonsterParser extends AchieveParser<MonsterAttackEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.PRESTRESS_LOSS_ATK_MONSTER;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return true;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, MonsterAttackEvent event) {
		int monsterType = event.getMosterType();
		if (monsterType != MonsterType.TYPE_1_VALUE && monsterType != MonsterType.TYPE_2_VALUE && monsterType != MonsterType.TYPE_11_VALUE) {
			HawkLog.logPrintln("MonsterAttackEvent update achieve failed, achieveType: {}, playerId: {}, monsterType: {}", 
					geAchieveType().getValue(), event.getPlayerId(), monsterType);
			return false;
		}
		int monsterLvl = event.getMonsterLevel();
		List<Integer> conditionValues = achieveConfig.getConditionValues();
		int minLvl = conditionValues.get(0);
		if (minLvl > 0 && monsterLvl < minLvl) {
			return false;
		}

		int count = achieveItem.getValue(0) + event.getAtkTimes();
		if (count >= conditionValues.get(1)) {
			count = conditionValues.get(1);
		}
		achieveItem.setValue(0, count);
		return true;
	}
	
	@Override
	public boolean isFinish(AchieveItem achieveItem, AchieveConfig achieveConfig) {
		int scoreValue = achieveItem.getValue(0);
		int configValue = achieveConfig.getConditionValue(1);
		return scoreValue >= configValue;
	}
}
