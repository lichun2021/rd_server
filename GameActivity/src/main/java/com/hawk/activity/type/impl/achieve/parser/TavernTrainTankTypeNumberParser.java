package com.hawk.activity.type.impl.achieve.parser;

import java.util.List;

import com.hawk.activity.event.impl.TrainSoldierCompleteEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.datatype.ListValueData;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * 指定列表中的兵种类型累计训练（训练完成）多少个 配置格式：兵种类型1_兵种类型2_数量
 * @author PhilChen
 *
 */
public class TavernTrainTankTypeNumberParser extends AchieveParser<TrainSoldierCompleteEvent> {
	
	private ListValueData listValueData = new ListValueData();

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.TAVERN_TRAIN_TANK_TYPE_NUMBER;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return true;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, TrainSoldierCompleteEvent event) {
		List<Integer> conditionValues = achieveConfig.getConditionValues();
		if (listValueData.isInList(conditionValues, event.getType()) == false) {
			return false;
		}
		int count = achieveItem.getValue(0) + event.getNum();
		achieveItem.setValue(0, count);
		return true;
	}

}
