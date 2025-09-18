package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.RechargeMoneyEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class RechargeGiftParser extends AchieveParser<RechargeMoneyEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.RECHARGE_GIFT;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return true;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, RechargeMoneyEvent event) {
		int configValue = achieveConfig.getConditionValue(0);
		int value = event.getMoney();
		int totalVal = achieveData.getValue(0)+ value;
		if (totalVal > configValue) {
			totalVal = configValue;
		}
		achieveData.setValue(0, totalVal);
		return true;
	}
}
