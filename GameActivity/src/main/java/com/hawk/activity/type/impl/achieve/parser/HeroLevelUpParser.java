package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.HeroLevelUpEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.helper.PlayerDataHelper;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.game.protocol.Hero.PBHeroInfo;

public class HeroLevelUpParser extends AchieveParser<HeroLevelUpEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.HERO_LEVEL_UP;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		ActivityDataProxy dataGeter = PlayerDataHelper.getInstance().getDataGeter();
		int heroId = achieveConfig.getConditionValue(0);
		int conditionVal = achieveConfig.getConditionValue(1);
		if (heroId == 0) {
			int num = dataGeter.getHeroNumByCondition(playerId, conditionVal, 0, 0);
			if (num >= 1) {
				achieveItem.setValue(0, conditionVal);
				return true;
			}
		} else {
			PBHeroInfo heroInfo = dataGeter.getHeroInfo(playerId, heroId);
			if (heroInfo == null) {
				return false;
			}
			achieveItem.setValue(0, Math.min(heroInfo.getLevel(), conditionVal));
			return true;
		}
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, HeroLevelUpEvent event) {
		int heroId = achieveConfig.getConditionValue(0);
		if (heroId > 0 && heroId != event.getHeroId()) {
			return false;
		}
		int level = achieveData.getValue(0);
		int newLevel = event.getLevel();
		if (level >= newLevel) {
			return false;
		}
		int configLevel = achieveConfig.getConditionValue(1);
		if (newLevel > configLevel) {
			newLevel = configLevel;
		}
		achieveData.setValue(0, newLevel);
		return true;
	}
}
