package com.hawk.activity.type.impl.achieve.parser;

import org.hawk.config.HawkConfigManager;
import org.hawk.result.Result;

import com.hawk.activity.event.impl.DragonBoatAchieveFinishEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.drogenBoatFestival.recharge.cfg.DragonBoatRechargeKVCfg;

public class DiamondRechargeCountDaysParser extends AchieveParser<DragonBoatAchieveFinishEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.DRAGON_BOAT_RECHARGE_ACHIVE_FINISH_COUNT;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return true;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, DragonBoatAchieveFinishEvent event) {
		DragonBoatRechargeKVCfg cfg = HawkConfigManager.getInstance().
				getKVInstance(DragonBoatRechargeKVCfg.class);
		if(event.getAchieveId() != cfg.getFinishId()){
			return false;
		}
		int count = event.getCount();
		int configValue = achieveConfig.getConditionValue(0);
		if (count >= configValue) {
			count = configValue;
		}
		achieveItem.setValue(0, count);
		return true;
	}
}
