package com.hawk.activity.type.impl.recallFriend.parser;

import com.hawk.activity.event.impl.RecallGuildFriendEvent;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * 发出召回盟友玩家.
 * @author hf
 */
public class GuildRecallFriendParser extends IGuildAchieveParser<RecallGuildFriendEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.RECALL_GUILD_FRIEND;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, RecallGuildFriendEvent event) {
		achieveData.setValue(0, achieveData.getValue(0) + 1);
		return true;
	}

}
