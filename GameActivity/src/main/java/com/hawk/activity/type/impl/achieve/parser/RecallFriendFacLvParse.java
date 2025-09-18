package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.RecallFriendEvent;
import com.hawk.activity.event.impl.RecalledFriendLoginEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * 发出召回玩家基地等级
 * @author hf
 *
 */
public class RecallFriendFacLvParse extends AchieveParser<RecalledFriendLoginEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.RECALL_FRIEND_LEVEL;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, RecalledFriendLoginEvent event) {
		int facLvCondition = achieveConfig.getConditionValue(0);
		if (event.getFacLv() < facLvCondition) {
			return false;
		}
		int configValue = achieveConfig.getConditionValue(1);
		int value = achieveItem.getValue(0) + 1;
		if (value > configValue) {
			value = configValue;
		}
		achieveItem.setValue(0, value);
		return true;
	}

}
