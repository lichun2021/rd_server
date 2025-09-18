package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.OccupyStrongpointFinishEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * 占领据点时长
 * @author Jesse
 *
 */
public class OccupyStongpointTimeParser extends AchieveParser<OccupyStrongpointFinishEvent>  {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.OCCUPY_STRONGPOINT_TIME;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, OccupyStrongpointFinishEvent event) {
		int afterNum = achieveItem.getValue(0) + event.getOccupyTime();
		int configNum = achieveConfig.getConditionValue(achieveConfig.getConditionValues().size() - 1);
		if (afterNum > configNum) {
			afterNum = configNum;
		}
		achieveItem.setValue(0, (int)afterNum);
		return true;
	}
}
