package com.hawk.activity.type.impl.achieve.parser;

import java.util.List;

import com.hawk.activity.event.impl.MonsterAttackEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.datatype.ListValueData;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * 消灭怪物*次数 配置格式：怪物id_次数
 * @author PhilChen
 *
 */
public class MonsterKillCountParser extends AchieveParser<MonsterAttackEvent> {

	private ListValueData listValueData = new ListValueData();
	
	@Override
	public AchieveType geAchieveType() {
		return AchieveType.MONSTER_KILL_COUNT;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, MonsterAttackEvent event) {
		if (!event.isKill()) {
			return false;
		}
		List<Integer> conditionValues = achieveConfig.getConditionValues();
		if (conditionValues.size() <= 0) {
			return false;
		}

		if (conditionValues.get(0) != 0 && listValueData.isInList(conditionValues, event.getMonsterId()) == false) {
			return false;
		}

		int count = achieveData.getValue(0) + event.getAtkTimes();
		achieveData.setValue(0, count);
		return true;
	}
}
