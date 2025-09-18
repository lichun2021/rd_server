package com.hawk.game.service.mssion.type;

import java.util.List;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventSoldierTrain;

@Mission(missionType = MissionType.SOLDIER_TRAIN_TYPE)
public class SoldierTrainTypeMission implements IMission {
	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventSoldierTrain event = (EventSoldierTrain) missionEvent;

		// 兵种id
		int soldierId = event.getSoldierId();
		BattleSoldierCfg bCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, soldierId);
		if (bCfg == null) {
			return;
		}
		int type = bCfg.getType();
		
		List<Integer> conditions = cfg.getIds();

		// conditions为空 或 为0 代表任意的
		if (!conditions.isEmpty() && !conditions.contains(0) && !conditions.contains(type)) {
			return;
		}

		int trainCount = event.getAfterCount() - event.getBeforeCount();
		entityItem.addValue(trainCount);

		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		
	}
}
