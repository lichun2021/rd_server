package com.hawk.activity.type.impl.achieve.parser;

import java.util.List;

import org.hawk.log.HawkLog;

import com.hawk.activity.event.impl.MonsterAttackEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.game.protocol.World.MonsterType;

/**
/* 攻击{1}等级的野怪{2}次				配置格式：怪物等级minLvl_maxLvl_次数
 * @author Jesse
 *
 */
public class AttackMonsterLvlParser extends AchieveParser<MonsterAttackEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.ATTACK_MONSTER_LEVEL;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, MonsterAttackEvent event) {
		int monsterType = event.getMosterType();
		if (monsterType != MonsterType.TYPE_1_VALUE && monsterType != MonsterType.TYPE_2_VALUE && monsterType != MonsterType.TYPE_7_VALUE && monsterType != MonsterType.TYPE_11_VALUE) {
			HawkLog.logPrintln("MonsterAttackEvent update achieve failed, achieveType: {}, playerId: {}, monsterType: {}", 
					geAchieveType().getValue(), event.getPlayerId(), monsterType);
			return false;
		}
		int monsterLvl = event.getMonsterLevel();
		List<Integer> conditionValues = achieveConfig.getConditionValues();
		int minLvl = conditionValues.get(0);
		int maxLvl = conditionValues.get(1);
		if (monsterLvl < minLvl || monsterLvl > maxLvl) {
			return false;
		}

		int count = achieveData.getValue(0) + event.getAtkTimes();
		achieveData.setValue(0, count);
		return true;
	}
}
