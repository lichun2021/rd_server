package com.hawk.game.service.mssion.type;

import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.MissionService;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventPvp;
import com.hawk.game.util.GsConst.MissionFunType;

/**
 * pvp胜利任务
 * 
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_PVP)
public class PvpMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventPvp event = (EventPvp) missionEvent;

		if (!event.isWin()) {
			return;
		}

		entityItem.addValue(1);
		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		EventPvp event = (EventPvp) missionEvent;
		MissionService.getInstance().missionRefreshAsync(player, MissionFunType.FUN_PVP_BATTLE, 0, 1);
		if(event.isWin()) {
			MissionService.getInstance().missionRefreshAsync(player, MissionFunType.FUN_PVP_WIN, 0, 1);
			MissionService.getInstance().missionRefreshAsync(player, MissionFunType.FUN_PVP_WIN, event.getConstrFactorLvl(), 1);
		}
	}
}
