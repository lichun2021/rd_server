package com.hawk.activity.type.impl.achieve.parser;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.hawk.activity.event.impl.SubmarineWarScoreEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.datatype.ListValueData;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class SubmarineWarKillParser extends AchieveParser<SubmarineWarScoreEvent> {

	private ListValueData listValueData = new ListValueData();
	
	@Override
	public AchieveType geAchieveType() {
		return AchieveType.SUBMARINE_WAR_KILL;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, SubmarineWarScoreEvent event) {
		Map<Integer, Integer> kmap = event.getKillMap();
		if(Objects.isNull(kmap)){
			return false;
		}
		if(kmap.size() <= 0){
			return false;
		}
		int count = achieveItem.getValue(0);
		int configShowValue = listValueData.getConfigShowValue(achieveConfig);
		List<Integer> conditionValues = achieveConfig.getConditionValues();
		for(Map.Entry<Integer, Integer> entry : kmap.entrySet()){
			int id = entry.getKey();
			int val = entry.getValue();
			if (listValueData.isInList(conditionValues, id)) {
				count += val;
			}
		}
		if (count > configShowValue) {
			count = configShowValue;
		}
		achieveItem.setValue(0, count);
		return true;
	}
}
