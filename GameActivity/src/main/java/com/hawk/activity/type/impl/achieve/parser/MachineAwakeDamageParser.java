package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.MachineAwakeAttackEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * 机甲觉醒伤害
 * @author Jesse
 *
 */
public class MachineAwakeDamageParser extends AchieveParser<MachineAwakeAttackEvent>  {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.AWAKE_DAMAGE;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, MachineAwakeAttackEvent event) {
		int afterNum = achieveItem.getValue(0) + event.getKillCnt();
		int configNum = achieveConfig.getConditionValue(achieveConfig.getConditionValues().size() - 1);
		if (afterNum > configNum) {
			afterNum = configNum;
		}
		achieveItem.setValue(0, (int)afterNum);
		return true;
	}
}