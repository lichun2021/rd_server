package com.hawk.activity.type.impl.achieve.parser;

import org.hawk.os.HawkOSOperator;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.JoinGuildEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class GuildJoinParse extends AchieveParser<JoinGuildEvent> {

	@Override
	public AchieveType geAchieveType() { 
		return AchieveType.GUILD_JOIN;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		ActivityDataProxy proxy = ActivityManager.getInstance().getDataGeter();
		String guildId = proxy.getGuildId(playerId);
		if(HawkOSOperator.isEmptyString(guildId)){
			return false;
		}
		int configValue = achieveConfig.getConditionValue(0);
		int value = achieveItem.getValue(0) + 1;
		if (value > configValue) {
			value = configValue;
		}
		achieveItem.setValue(0, value);
		return true;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, JoinGuildEvent event) {
		int configValue = achieveConfig.getConditionValue(0);
		int value = achieveItem.getValue(0) + 1;
		if (value > configValue) {
			value = configValue;
		}
		achieveItem.setValue(0, value);
		return true;
	}
}
