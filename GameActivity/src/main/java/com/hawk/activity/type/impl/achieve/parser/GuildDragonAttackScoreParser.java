package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.GuildDragonAttackScoreMaxEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class GuildDragonAttackScoreParser extends AchieveParser<GuildDragonAttackScoreMaxEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.GUILD_DRAGON_ATTACK_SCORE;
	}
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return true;
	}
	@Override
	protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig,
			GuildDragonAttackScoreMaxEvent event) {
		int configValue = achieveConfig.getConditionValue(0);
		int value = event.getScore();
		if (value >= configValue) {
			value = configValue;
		}
		achieveData.setValue(0, value);
		return true;
	}
}
