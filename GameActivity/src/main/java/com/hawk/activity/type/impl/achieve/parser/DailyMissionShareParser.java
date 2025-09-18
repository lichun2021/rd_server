package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.DailyMissionShareEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class DailyMissionShareParser extends AchieveParser<DailyMissionShareEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.DAILY_SHARE;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig,
			DailyMissionShareEvent event) {
		if(event.getShareType() == null){ //不合法的分享类型
			return false;
		}
		achieveData.setValue(0, achieveData.getValue(0) + 1);
		return true;
	}
}
