package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.RechargeWelfareLotteryEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * 充值福利抽奖次数
 */
public class RechargeWelfareLotteryParser extends AchieveParser<RechargeWelfareLotteryEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.RECHARGE_WELFARE_LOTTERY_TIMES;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return true;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, RechargeWelfareLotteryEvent event) {
		int configValue = achieveConfig.getConditionValue(0);
		int value = event.getCount(); // 优化后传的是全量
		value = Math.min(value, configValue);
		achieveItem.setValue(0, value);
		return true;
	}

}
