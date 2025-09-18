package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.ItemUseResCollectBufEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class ItemUseResCollectBufParser extends AchieveParser<ItemUseResCollectBufEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.UPGRADE_RESOURCE_PRODUCTOR;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, ItemUseResCollectBufEvent event) {
//		int configValue = achieveConfig.getConditionValue(0);
//		int value = event.getFightPower();
//		if (value > configValue) {
//			value = configValue;
//		}
//		achieveItem.setValue(0, value);
		return false;
	}
}
