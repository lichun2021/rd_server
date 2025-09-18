package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.HeroShareEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * 玩家拥有并已分享指定英雄1次		配置格式: 英雄id 
 * 
 * @author Jesse
 */
public class HeroShareParser extends AchieveParser<HeroShareEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.HERO_SHARE;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, HeroShareEvent event) {
		int conditionId = achieveConfig.getConditionValue(0);
		if (conditionId != 0 && event.getHeroId() != conditionId) {
			return false;
		}
		achieveItem.setValue(0, 1);
		return true;
	}
}
