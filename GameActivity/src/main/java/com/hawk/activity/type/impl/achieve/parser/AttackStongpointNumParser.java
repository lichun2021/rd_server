package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.OccupyStrongpointEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * 攻打据点
 * @author Jesse
 *
 */
public class AttackStongpointNumParser extends AchieveParser<OccupyStrongpointEvent>  {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.ATTACK_STRONGPOINT_NUM;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, OccupyStrongpointEvent event) {
		int afterNum = achieveItem.getValue(0) + 1;
		int configNum = achieveConfig.getConditionValue(achieveConfig.getConditionValues().size() - 1);
		if (afterNum > configNum) {
			afterNum = configNum;
		}
		achieveItem.setValue(0, (int)afterNum);
		return true;
	}
}