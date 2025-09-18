package com.hawk.activity.type.impl.achieve.parser;

import org.hawk.os.HawkTime;
import com.hawk.activity.event.impl.GoldBabyOnlineTimeEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class GoldBabyOnlineTimeParser extends AchieveParser<GoldBabyOnlineTimeEvent> {
	
	@Override
	public AchieveType geAchieveType() {
		return AchieveType.GOLD_BABY_DAILY_ONLINE;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, GoldBabyOnlineTimeEvent event) {
		long lastLoginTime = event.getLastLoginTime();
		long now = HawkTime.getMillisecond();
		int todayOnlineSecond = achieveItem.getValue(0);
		if (todayOnlineSecond >= achieveConfig.getConditionValue(0)) {
			return false;
		}
		int addSecond = Integer.valueOf(((now-lastLoginTime)/1000)+"");
		todayOnlineSecond += addSecond;
		if (todayOnlineSecond > achieveConfig.getConditionValue(0)) {
			todayOnlineSecond = achieveConfig.getConditionValue(0);
		}
		achieveItem.setValue(0, todayOnlineSecond);
		return true;
	}
}
