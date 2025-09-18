package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.HeroUpStarEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.helper.PlayerDataHelper;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.game.protocol.Hero.PBHeroInfo;
/**
 * 指定英雄达到x星 					配置格式：英雄id_星级
 * @author Jesse
 *
 */
public class HeroStarUpParser extends AchieveParser<HeroUpStarEvent> {
	
	@Override
	public AchieveType geAchieveType() {
		return AchieveType.HERO_STAR_UP_TWO;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		int conditionId = achieveConfig.getConditionValue(0);
		int conditionVal = achieveConfig.getConditionValue(1);
		ActivityDataProxy dataGeter = PlayerDataHelper.getInstance().getDataGeter();
		PBHeroInfo heroInfo = dataGeter.getHeroInfo(playerId, conditionId);
		if(heroInfo == null){
			return false;
		}
		achieveItem.setValue(0, Math.min(heroInfo.getStar(), conditionVal));
		return true;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, HeroUpStarEvent event) {
		int conditionId = achieveConfig.getConditionValue(0);
		int conditionVal = achieveConfig.getConditionValue(1);
		if (event.getHeroId() != conditionId) {
			return false;
		}
		int star = event.getNewStar();
		star = Math.min(star, conditionVal);
		int value = achieveItem.getValue(0);
		if (value >= star) {
			return false;
		}
		achieveItem.setValue(0, star);
		return true;
	}
}
