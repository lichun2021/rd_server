package com.hawk.activity.type.impl.achieve.parser;

import java.util.Optional;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.VitCostEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.spread.SpreadActivity;
import com.hawk.activity.type.impl.spread.entity.SpreadEntity;
import com.hawk.game.protocol.Activity.ActivityType;

public class SpreadNewPlayerCostVitParser extends AchieveParser<VitCostEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.SPREAD_VIT_COST;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, VitCostEvent event) {
		Optional<ActivityBase> opt = ActivityManager.getInstance().getActivity(ActivityType.SPREAD_VALUE);
		if (!opt.isPresent()) {
			return false;
		}

		Optional<SpreadEntity> opEntity = ((SpreadActivity) opt.get()).getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return false;
		}
		SpreadEntity entity = opEntity.get();
		if (entity.getIsBindCode()) {

			int configValue = achieveConfig.getConditionValue(0);
			int value = event.getCost() + achieveItem.getValue(0);
			if (value > configValue) {
				value = configValue;
			}
			achieveItem.setValue(0, value);
		}
		return true;
	}
}
