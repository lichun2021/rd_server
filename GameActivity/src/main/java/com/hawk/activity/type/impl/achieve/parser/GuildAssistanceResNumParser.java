package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.GuildAssistanceEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class GuildAssistanceResNumParser extends AchieveParser<GuildAssistanceEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.GUILD_ASSISTANCE_RES_NUM;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	
	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, GuildAssistanceEvent event) {
		achieveItem.setValue(0, achieveItem.getValue(0) + 1);
		return true;
	}
}
