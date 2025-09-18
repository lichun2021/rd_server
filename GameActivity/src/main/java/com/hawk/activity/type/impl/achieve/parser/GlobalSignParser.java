package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.GlobalSignCountEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * 装备打造/升阶/升品
 * @author golden
 *
 */
public class GlobalSignParser extends AchieveParser<GlobalSignCountEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.GLOBAL_SIGN_COUNT;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, GlobalSignCountEvent event) {
		int count = event.getCount();
		int configValue = achieveConfig.getConditionValue(0);
		if (count >= configValue) {
			count = configValue;
		}
		achieveData.setValue(0, count);
		return true;
	}
}
