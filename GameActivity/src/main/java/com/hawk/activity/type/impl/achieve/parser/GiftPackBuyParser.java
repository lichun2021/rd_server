package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.ItemBuyEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class GiftPackBuyParser extends AchieveParser<ItemBuyEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.GIFT_PACK_BUG;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, ItemBuyEvent event) {
		int giftId = achieveConfig.getConditionValue(0);
		if (giftId != event.getItemId()) {
			return false;
		}
		int configValue = achieveConfig.getConditionValue(1);
		int value = achieveItem.getValue(0) + event.getBuyNum();
		if (value > configValue) {
			value = configValue;
		}
		achieveItem.setValue(0, value);
		return true;
	}
}
