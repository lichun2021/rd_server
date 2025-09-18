

package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.WLQDShareEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * 威龙庆典分享
 * @author lating
 *
 */
public class WLQDShareParse  extends AchieveParser<WLQDShareEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.WEILONG_QINGDIAN_SHARE;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig,
			WLQDShareEvent event) {
		int configValue = achieveConfig.getConditionValue(0);
		int value = achieveData.getValue(0) + 1;
		if (value > configValue) {
			value = configValue;
		}
		achieveData.setValue(0, value);
		return true;
	}
	
}
