package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.DiamondRechargeEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class DiamondRechargeScoreParser extends AchieveParser<DiamondRechargeEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.DIAMOND_RECHARGE_SCORE;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return true;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, DiamondRechargeEvent event) {
		int configValue = achieveConfig.getConditionValue(0);
		int value = event.getDiamondNum();
		int totalVal = achieveItem.getValue(0)+ value;
		if (totalVal > configValue) {
			totalVal = configValue;
		}
		achieveItem.setValue(0, totalVal);
		return true;
	}
}
