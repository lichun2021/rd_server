package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.QuestionShareOkEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * 
 * @author RickMei 在线答题 红警经典问题分享 成就解析
 */
public class QuestionShareTimesParser extends AchieveParser<QuestionShareOkEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.QUESTION_SHARE_TIMES;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return true;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, QuestionShareOkEvent event) {
		int configValue = achieveConfig.getConditionValue(0);
		int value = achieveData.getValue(0) + 1;
		if (value > configValue) {
			value = configValue;
		}
		achieveData.setValue(0, value);
		return true;
	}
}
