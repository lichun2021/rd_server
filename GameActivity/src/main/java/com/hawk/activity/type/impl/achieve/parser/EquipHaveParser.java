package com.hawk.activity.type.impl.achieve.parser;

import java.util.List;

import com.hawk.activity.event.impl.EquipChangeEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.helper.PlayerDataHelper;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/** 拥有{0}品质{1}等级装备{2}个 配置格式：品质_等级_数量 (品质/等级配0表示任意条件) */
public class EquipHaveParser extends AchieveParser<EquipChangeEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.HAVE_EQUIP_COUNT;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return updateAchieveInfo(playerId, achieveConfig, achieveItem);
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, EquipChangeEvent event) {
		return updateAchieveInfo(event.getPlayerId(), achieveConfig, achieveItem);
	}

	private boolean updateAchieveInfo(String playerId, AchieveConfig achieveConfig, AchieveItem achieveItem) {
		List<Integer> conditionValues = achieveConfig.getConditionValues();
		int quality = conditionValues.get(0);
		int level = conditionValues.get(1);
		ActivityDataProxy dataGeter = PlayerDataHelper.getInstance().getDataGeter();
		int value = dataGeter.getEquipNumByCondition(playerId, level, quality);
		if (value <= achieveItem.getValue(0)) {
			return false;
		}
		achieveItem.setValue(0, value);
		return true;
	}
}
