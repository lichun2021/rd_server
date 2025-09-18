package com.hawk.activity.type.impl.achieve.parser;

import java.util.Optional;

import org.hawk.os.HawkOSOperator;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.BattlePointChangeEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.helper.PlayerDataHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.mergecompetition.MergeCompetitionActivity;

public class MergeCompeteGuildNoArmyPowerParser extends AchieveParser<BattlePointChangeEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.MERGE_COMPETE_GUILD_NOARMY_POWER;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		ActivityDataProxy dataGeter = PlayerDataHelper.getInstance().getDataGeter();
		String guildId = dataGeter.getGuildId(playerId);
		if (HawkOSOperator.isEmptyString(guildId)) {
			return false;
		}
		
		long noarmypower = dataGeter.getGuildNoArmyPower(guildId);
		int configValue = achieveConfig.getConditionValue(0);
		if (noarmypower >= configValue) {
			noarmypower = configValue;
		}
		achieveItem.setValue(0, (int)noarmypower);
		return true;
	}

	
	@Override
	protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, BattlePointChangeEvent event) {
		ActivityDataProxy dataGeter = PlayerDataHelper.getInstance().getDataGeter();
		String guildId = dataGeter.getGuildId(event.getPlayerId());
		if (HawkOSOperator.isEmptyString(guildId)) {
			return false;
		}
		
		int configValue = achieveConfig.getConditionValue(0);
		long noarmypower = dataGeter.getGuildNoArmyPower(guildId);
		if (noarmypower >= configValue) {
			noarmypower = configValue;
		}
		achieveData.setValue(0, (int)noarmypower);
		Optional<MergeCompetitionActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.MERGE_COMPETITION.intValue());
		if (opActivity.isPresent()) {
			opActivity.get().refreshGuildPowerTargetTime(event.getPlayerId());
		}
		return true;
	}
	
}
