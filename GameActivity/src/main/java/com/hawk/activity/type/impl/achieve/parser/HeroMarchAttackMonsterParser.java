package com.hawk.activity.type.impl.achieve.parser;

import org.hawk.log.HawkLog;

import com.hawk.activity.event.impl.MonsterAttackEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.game.protocol.World.MonsterType;

/**
 * 注意, 因为集结行军攻击的是boss 不在该任务类型之中, 且战斗那边，在集结行军那里不好取英雄信息,所以在那边没有处理英雄信息.
 * @author jm
 *
 */
public class HeroMarchAttackMonsterParser extends AchieveParser<MonsterAttackEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.HERO_MARCH_KILL_MONSTER;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return true;
	}
	@Override
	protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, MonsterAttackEvent event) {
		int monsterType = event.getMosterType();
		if (monsterType != MonsterType.TYPE_1_VALUE && monsterType != MonsterType.TYPE_2_VALUE && monsterType != MonsterType.TYPE_7_VALUE && monsterType != MonsterType.TYPE_11_VALUE) {
			HawkLog.logPrintln("MonsterAttackEvent update achieve failed, achieveType: {}, playerId: {}, monsterType: {}", 
					geAchieveType().getValue(), event.getPlayerId(), monsterType);
			return false;
		}
		if (!event.isWithHero() || !event.isKill()) {
			return false;
		}
		
		int configValue = achieveConfig.getConditionValue(0);
		int newValue = achieveData.getValue(0) + event.getAtkTimes();
		newValue = Math.min(configValue, newValue);
		achieveData.setValue(0, newValue);
		return true;
	}

}
