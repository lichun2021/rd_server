package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.ResourceCollectEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * 采集次数成就解析器
 * @author golden
 *
 */
public class ResourceCollectTimesParser extends AchieveParser<ResourceCollectEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.RES_COLLECT_TIMES;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, ResourceCollectEvent event) {
		int count = achieveData.getValue(0) + 1;
		achieveData.setValue(0, count);
		return true;
	}
}
