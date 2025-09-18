package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.UpgradeResourceProductorEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * 增加矿石产量
 * 
 * @author golden
 *
 */
public class UpgradeResourceProductorParser extends AchieveParser<UpgradeResourceProductorEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.UPGRADE_RESOURCE_PRODUCTOR;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return true;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, UpgradeResourceProductorEvent event) {
		int configEffectId = achieveConfig.getConditionValue(0);
		if (configEffectId > 0 && event.getEffectId() != configEffectId) {
			return false;
		}
		int oldCount = achieveItem.getValue(0);
		achieveItem.setValue(0, oldCount + 1);
		return true;
	}
}
