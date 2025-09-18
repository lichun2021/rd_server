package com.hawk.activity.type.impl.achieve.parser;

import org.hawk.os.HawkTime;

import com.hawk.activity.event.impl.QueueSpeedUpEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class SpeedUpTimeTotalParser extends AchieveParser<QueueSpeedUpEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.SPEED_UP_TIME_TOTAL;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, QueueSpeedUpEvent event) {
		int queueType = achieveConfig.getConditionValue(0);
		if (queueType > 0 && event.getQueueType() != queueType) {
			return false;
		}
		int m = (int) Math.ceil(event.getUpTime() / (double) HawkTime.MINUTE_MILLI_SECONDS);
		if (m < 0) {
			logger.error("speed up achieve parser update error! m={} upTime={}", m, event.getUpTime());
			return false;
		}
		int configValue = achieveConfig.getConditionValue(1);
		int value = achieveItem.getValue(0) + m;
		if (value > configValue) {
			value = configValue;
		}
		achieveItem.setValue(0, value);
		return true;
	}
	
}
