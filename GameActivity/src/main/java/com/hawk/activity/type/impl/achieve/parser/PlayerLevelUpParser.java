package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.PlayerLevelUpEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.helper.PlayerDataHelper;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class PlayerLevelUpParser extends AchieveParser<PlayerLevelUpEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.PLAYER_LEVEL_UP;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		ActivityDataProxy dataGeter = PlayerDataHelper.getInstance().getDataGeter();
		int playerLevel = dataGeter.getPlayerLevel(playerId);
		int configPlayerLevel = achieveConfig.getConditionValue(0);
		if (playerLevel > configPlayerLevel) {
			playerLevel = configPlayerLevel;
		}
		achieveItem.setValue(0, playerLevel);
		return true;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, PlayerLevelUpEvent event) {
		achieveData.setValue(0, event.getLevel());
		return true;
	}
}
