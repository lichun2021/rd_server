package com.hawk.game.service.mssion.type;

import java.util.List;

import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.MissionService;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventSoldierTrain;
import com.hawk.game.util.GsConst.MissionFunType;

/**
 * 士兵训练完成任务
 * 
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_SOLDIER_TRAIN)
public class SoldierTrainMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventSoldierTrain event = (EventSoldierTrain) missionEvent;

		// 兵种id
		int soldierId = event.getSoldierId();
		List<Integer> conditions = cfg.getIds();

		// conditions为空 或 为0 代表任意的
		if (!conditions.isEmpty() && !conditions.contains(0) && !conditions.contains(soldierId)) {
			return;
		}

		int trainCount = event.getAfterCount() - event.getBeforeCount();
		entityItem.addValue(trainCount);

		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		EventSoldierTrain event = (EventSoldierTrain) missionEvent;
		int count = event.getAfterCount() - event.getBeforeCount();
		MissionService.getInstance().missionRefreshAsync(player, MissionFunType.FUN_TRAIN_SOLDIER_COMPLETE_NUMBER, event.getSoldierId(), count);
	}
}
