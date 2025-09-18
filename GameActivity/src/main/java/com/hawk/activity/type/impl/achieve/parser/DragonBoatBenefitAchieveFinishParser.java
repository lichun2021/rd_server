package com.hawk.activity.type.impl.achieve.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.event.impl.DragonBoatBenefitAchieveFinishEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class DragonBoatBenefitAchieveFinishParser extends AchieveParser<DragonBoatBenefitAchieveFinishEvent> {
	private static final Logger logger = LoggerFactory.getLogger("Server");
	@Override
	public AchieveType geAchieveType() {
		return AchieveType.DRAGON_BOAT_BENEFIT_ACHIEVE_FINISH;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, DragonBoatBenefitAchieveFinishEvent event) {
		int configValue = achieveConfig.getConditionValue(0);
		int value = achieveItem.getValue(0);
		int finishNum = event.getFinishNum();
		int total = value + finishNum;
		if (total > configValue) {
			total = configValue;
		}
		achieveItem.setValue(0, total);
		logger.info("updateAchieve,onAchieveFinished,playerId:{},achieveId:{},value:{},config:{},after:{}",
				event.getPlayerId(),achieveItem.getAchieveId(),value,configValue,total);
		return true;
	}
}
