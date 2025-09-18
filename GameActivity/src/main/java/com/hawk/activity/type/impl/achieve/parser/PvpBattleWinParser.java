package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.PvpBattleEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class PvpBattleWinParser extends AchieveParser<PvpBattleEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.PVP_BATTLE_WIN_NUM;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, PvpBattleEvent event) {
		if (event.isAtkWin() == false) {
			return false;
		}
		achieveItem.setValue(0, achieveItem.getValue(0) + 1);
		return true;
	}
}
