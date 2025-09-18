package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.RecallFriendEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * 发出召回玩家.
 * @author jm
 *
 */
public class RecallFriendParse extends AchieveParser<RecallFriendEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.RECALL_FRIEND;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, RecallFriendEvent event) {
		achieveData.setValue(0, achieveData.getValue(0) + 1);
		
		return true;
	}

}
