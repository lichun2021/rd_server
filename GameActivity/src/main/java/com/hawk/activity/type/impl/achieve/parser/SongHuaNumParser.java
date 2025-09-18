package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.SendFlowerEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * 机甲觉醒伤害
 * @author Jesse
 *
 */
public class SongHuaNumParser extends AchieveParser<SendFlowerEvent>  {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.SONG_HUA;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, SendFlowerEvent event) {
		int afterNum = achieveItem.getValue(0) + event.getNum();
		int configNum = achieveConfig.getConditionValue(achieveConfig.getConditionValues().size() - 1);
		if (afterNum > configNum) {
			afterNum = configNum;
		}
		achieveItem.setValue(0, (int)afterNum);
		return true;
	}
}