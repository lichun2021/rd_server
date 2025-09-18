package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.UseItemSpeedUpEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * 使用道具加速x分钟
 * @author golden
 *
 */
public class UseItemSpeedParser extends AchieveParser<UseItemSpeedUpEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.USE_ITEM_SPEED_UP;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, UseItemSpeedUpEvent event) {
		
		int configValue = achieveConfig.getConditionValue(0);
		int value = achieveItem.getValue(0) + event.getMinute();
		if (value > configValue) {
			value = configValue;
		}
		achieveItem.setValue(0, value);
		return true;
	}
}
