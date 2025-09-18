package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.HeroShareEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.helper.PlayerDataHelper;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * 玩家拥有并已分享的英雄x个 配置格式: 数量
 * 
 * @author Jesse
 */
public class HeroShareNumParser extends AchieveParser<HeroShareEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.HERO_SHARE_NUM;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return updateAchieveInfo(playerId, achieveConfig, achieveItem);
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, HeroShareEvent event) {
		return updateAchieveInfo(event.getPlayerId(), achieveConfig, achieveItem);
	}

	private boolean updateAchieveInfo(String playerId, AchieveConfig achieveConfig, AchieveItem achieveItem) {
		ActivityDataProxy dataGeter = PlayerDataHelper.getInstance().getDataGeter();
		int value = dataGeter.getSharedHeroNum(playerId);
		if (value <= achieveItem.getValue(0)) {
			return false;
		}
		achieveItem.setValue(0, value);
		return true;
	}
}
