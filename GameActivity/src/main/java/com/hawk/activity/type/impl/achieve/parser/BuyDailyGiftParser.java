package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.BuyItemConsumeEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.dressup.energygather.cfg.EnergyGatherActivityKVCfg;
import org.hawk.config.HawkConfigManager;

/**
 * 购买每日必买礼包次数
 */
public class BuyDailyGiftParser extends AchieveParser<BuyItemConsumeEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.BUY_DAILY_GIFT;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, BuyItemConsumeEvent event) {
		int configValue = achieveConfig.getConditionValue(0);
		//针对271 活动
		EnergyGatherActivityKVCfg cfg =  HawkConfigManager.getInstance().getKVInstance(EnergyGatherActivityKVCfg.class);
		int value = cfg.getGiftNumMap().getOrDefault(event.getGiftId(), 0);
		int totalVal = achieveItem.getValue(0) + value;
		if (totalVal > configValue) {
			totalVal = configValue;
		}
		achieveItem.setValue(0, totalVal);
		return true;
	}
}
