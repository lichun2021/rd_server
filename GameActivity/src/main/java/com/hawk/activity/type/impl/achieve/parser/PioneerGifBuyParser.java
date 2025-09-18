package com.hawk.activity.type.impl.achieve.parser;

import org.hawk.os.HawkTime;
import com.hawk.activity.event.impl.PioneerGifBuyTimesEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class PioneerGifBuyParser extends AchieveParser<PioneerGifBuyTimesEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.PIONEER_GIFT_BUY_DAYS;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return true;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, PioneerGifBuyTimesEvent event) {
		Integer days = achieveItem.getValue(0);      // 累计购买天数
		Integer lastOpTime = achieveItem.getValue(1);  // 上次更新的时间
		
		int nowSecond = HawkTime.getSeconds();
		if (days < achieveConfig.getConditionValue(0) && 
				!HawkTime.isSameDay(lastOpTime * 1000L, nowSecond * 1000L)) {
			achieveItem.setValue(0, days + 1);
			achieveItem.setValue(1, nowSecond);
			return true;
		}
		
		return false;
	}
}
