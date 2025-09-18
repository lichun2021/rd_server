package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.OccupyStrongpointEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * 占领据点
 * @author Jesse
 *
 */
public class OccupyStongpointNumParser extends AchieveParser<OccupyStrongpointEvent>  {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.OCCUPY_STRONGPOINT_NUM;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, OccupyStrongpointEvent event) {
		if (!event.isAtkWin()) {
			return true;
		}
		int afterNum = achieveItem.getValue(0) + 1;
		int configNum = achieveConfig.getConditionValue(achieveConfig.getConditionValues().size() - 1);
		if (afterNum > configNum) {
			afterNum = configNum;
		}
		achieveItem.setValue(0, (int)afterNum);
		return true;
	}
}
