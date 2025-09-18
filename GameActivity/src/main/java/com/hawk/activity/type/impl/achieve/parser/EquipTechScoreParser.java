package com.hawk.activity.type.impl.achieve.parser;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.EquipTechScoreEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.equipTech.cfg.EquipTechActivityKVConfig;

public class EquipTechScoreParser extends AchieveParser<EquipTechScoreEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.EQUIP_TECH_SCORE;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		int gachaCount = ActivityManager.getInstance().getDataGeter().getAmourGachaCount(playerId);
		EquipTechActivityKVConfig cfg = HawkConfigManager.getInstance().getKVInstance(EquipTechActivityKVConfig.class);
		int addScore = cfg.getGetScore() * gachaCount;
		achieveItem.setValue(0, addScore);
		return true;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, EquipTechScoreEvent event) {
		int count = achieveItem.getValue(0) + event.getScore();
		achieveItem.setValue(0, count);
		return true;
	}
}
