package com.hawk.activity.type.impl.achieve.parser;

import java.util.List;

import com.hawk.activity.event.impl.HeroChangeEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.helper.PlayerDataHelper;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * 总共拥有英雄{0}级{1}品质{2}星级的英雄{3}个 配置格式: 等级_品质_星级_数量 (等级/品质/星级配0标识任意条件)
 * 
 * @author Jesse
 */
public class HeroHaveNumParser extends AchieveParser<HeroChangeEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.HERO_HAVE_NUM;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return updateAchieveInfo(playerId, achieveConfig, achieveItem);
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, HeroChangeEvent event) {
		return updateAchieveInfo(event.getPlayerId(), achieveConfig, achieveItem);
	}

	private boolean updateAchieveInfo(String playerId, AchieveConfig achieveConfig, AchieveItem achieveItem) {
		List<Integer> conditionValues = achieveConfig.getConditionValues();
		int lvlLimit = conditionValues.get(0);
		int qualityLimit = conditionValues.get(1);
		int starLimit = conditionValues.get(2);
		ActivityDataProxy dataGeter = PlayerDataHelper.getInstance().getDataGeter();
		int value = dataGeter.getHeroNumByCondition(playerId, lvlLimit, qualityLimit, starLimit);
		if (value <= achieveItem.getValue(0)) {
			return false;
		}
		achieveItem.setValue(0, value);
		return true;
	}
}
