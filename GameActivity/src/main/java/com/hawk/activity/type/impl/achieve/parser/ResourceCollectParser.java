package com.hawk.activity.type.impl.achieve.parser;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.hawk.activity.event.impl.ResourceCollectEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.datatype.ListValueData;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * 资源采集成就解析器
 * @author golden
 *
 */
public class ResourceCollectParser extends AchieveParser<ResourceCollectEvent> {

	private ListValueData listValueData = new ListValueData();
	
	@Override
	public AchieveType geAchieveType() {
		return AchieveType.RESOURCE_COLLECT;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, ResourceCollectEvent event) {
		
		double addNum = 0;
		int configNum = achieveConfig.getConditionValue(achieveConfig.getConditionValues().size() - 1);
		
		// 任意资源成就项
		if (achieveConfig.getConditionValue(0) == 0) {
			Map<Integer, Double> collectMap = event.getCollectMap();
			for (Entry<Integer, Double> collectEntry: collectMap.entrySet()) {
				addNum = collectEntry.getValue() * event.getResWeight(collectEntry.getKey());
			}
			
			// 指定资源成就项
		} else {
			List<Integer> conditionValues = achieveConfig.getConditionValues();
			
			Set<Integer> resourceTypes = event.getCollectMap().keySet();
			for (int resourceType : resourceTypes) {
				if (!listValueData.isInList(conditionValues, resourceType)) {
					return false;
				}
				Double collectNum = event.getCollectNum(resourceType);
				if (collectNum == null || collectNum <= 0) {
					return false;
				}
				addNum = event.getCollectNum(resourceType);
			}
		}
		
		int afterNum = (int)addNum + achieveItem.getValue(0); 
		if (afterNum > configNum) {
			afterNum = configNum;
		}
		achieveItem.setValue(0, (int)afterNum);
		return true;
	}
}
