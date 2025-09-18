

package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.ReturnPuzzleShareEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * 回流拼图分享
 * @author lating
 *
 */
public class ReturnPuzzleShareParse  extends AchieveParser<ReturnPuzzleShareEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.RETURN_PUZZLE_SHARE;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig,
			ReturnPuzzleShareEvent event) {
		int configValue = achieveConfig.getConditionValue(0);
		int value = event.getShareId();
		if (configValue == value && value != achieveData.getValue(0)) {			
			achieveData.setValue(0, value);
			return true;
		}else{
			return false;
		}
	}
	
}
