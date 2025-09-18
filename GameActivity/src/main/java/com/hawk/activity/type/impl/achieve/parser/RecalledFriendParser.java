package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.RecalledFriendLoginEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * 每日召回玩家.
 * @author jm
 *
 */
public class RecalledFriendParser extends AchieveParser<RecalledFriendLoginEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.RECALLED_FIREND;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig,
			RecalledFriendLoginEvent event) {
		achieveData.setValue(0, achieveData.getValue(0) + 1);
		return true;
	}

}
