package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.ShareEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class ShareTypeParser extends AchieveParser<ShareEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.SHARE_TYPE;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, ShareEvent event) {
		if (event.getShareType() == null) { // 不合法的分享类型
			return false;
		}
		int typeLimit = achieveConfig.getConditionValue(0);
		if (typeLimit > 0 && event.getShareType().getNumber() != typeLimit) {
			return false;
		}
		int val = achieveData.getValue(0) + 1;
		int configNum = achieveConfig.getConditionValue(achieveConfig.getConditionValues().size() - 1);
		val = Math.min(val, configNum);
		achieveData.setValue(0, val);
		return true;
	}
}
