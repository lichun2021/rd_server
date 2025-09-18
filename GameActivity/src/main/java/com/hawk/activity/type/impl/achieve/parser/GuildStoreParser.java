package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.GuildStoreEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * 开启联盟宝藏
 * @author golden
 *
 */
public class GuildStoreParser extends AchieveParser<GuildStoreEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.GUILD_STORE;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, GuildStoreEvent event) {
		achieveItem.setValue(0, achieveItem.getValue(0) + 1);
		return true;
	}
}
