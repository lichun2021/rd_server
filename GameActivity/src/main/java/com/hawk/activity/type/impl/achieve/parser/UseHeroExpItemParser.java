package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.UseHeroExpItemEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * 培养英雄(使用英雄经验道具)
 * @author golden
 *
 */
public class UseHeroExpItemParser extends AchieveParser<UseHeroExpItemEvent>  {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.USE_HERO_EXPITEM;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, UseHeroExpItemEvent event) {
		int afterNum = achieveData.getValue(0) + event.getCount();
		achieveData.setValue(0, (int)afterNum);
		return true;
	}
}
