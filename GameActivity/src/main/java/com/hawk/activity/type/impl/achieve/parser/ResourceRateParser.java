package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.ResourceRateChangeEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.helper.PlayerDataHelper;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class ResourceRateParser extends AchieveParser<ResourceRateChangeEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.RESOURCE_RATE;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		ActivityDataProxy dataGeter = PlayerDataHelper.getInstance().getDataGeter();
		int resType = achieveConfig.getConditionValue(0);
		long resourceOutputRate = dataGeter.getResourceOutputRate(playerId, resType);
		int configNum = achieveConfig.getConditionValue(1);
		if (resourceOutputRate > configNum) {
			resourceOutputRate = configNum;
		}
		achieveItem.setValue(0, (int) resourceOutputRate);
		return true;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, ResourceRateChangeEvent event) {
		int resourceType = achieveConfig.getConditionValue(0);
		if (resourceType != event.getResourceType()) {
			return false;
		}
		int configNum = achieveConfig.getConditionValue(1);
		int num = (int) (event.getAddRate() + achieveItem.getValue(0));
		if (num > configNum) {
			num = configNum;
		}
		achieveItem.setValue(0, num);
		return true;
	}
}
