package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.EquipUpEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * 装备打造/升阶/升品
 * @author golden
 *
 */
public class EquipUpParser extends AchieveParser<EquipUpEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.EQUIP_UP;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, EquipUpEvent event) {
		int afterNum = achieveData.getValue(0) + event.getCount();
		int configNum = achieveConfig.getConditionValue(achieveConfig.getConditionValues().size() - 1);
		if (afterNum > configNum) {
			afterNum = configNum;
		}
		achieveData.setValue(0, (int)afterNum);
		return true;
	}
}
