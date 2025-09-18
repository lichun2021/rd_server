package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.RandomHeroEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.fullyArmed.cfg.FullyArmedAchieveCfg;
import com.hawk.game.protocol.Const.GachaType;

public class RandomHeroParser extends AchieveParser<RandomHeroEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.RANDOM_HERO;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, RandomHeroEvent event) {
		if(achieveConfig instanceof FullyArmedAchieveCfg){
			if(GachaType.ARMOUR_ONE_VALUE != event.getGachaType() && GachaType.ARMOUR_TEN_VALUE != event.getGachaType()){
				return false;
			}
		}
		int configValue = achieveConfig.getConditionValue(0);
		int value = achieveItem.getValue(0) + event.getCount();
		if (value > configValue) {
			value = configValue;
		}
		achieveItem.setValue(0, value);
		return true;
	}
}
