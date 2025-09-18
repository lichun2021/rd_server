package com.hawk.game.service.mssion.type;

import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.MissionService;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventOccupyStrongpointTime;
import com.hawk.game.util.GsConst.MissionFunType;

/**
 * 占领{1}等级以上的据点{2}时长
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_OCCUPY_STRONGPOINT_TIME)
public class OccupyStrongpointTimeMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		EventOccupyStrongpointTime event = (EventOccupyStrongpointTime)missionEvent;
		MissionService.getInstance().missionRefreshAsync(player, MissionFunType.FUN_OCCUPY_STRONGPOINT, 0, event.getTime());
		MissionService.getInstance().missionRefreshAsync(player, MissionFunType.FUN_OCCUPY_STRONGPOINT, event.getLevel(), event.getTime());
	}

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventOccupyStrongpointTime event = (EventOccupyStrongpointTime)missionEvent;
		if (event.getLevel() < cfg.getIds().get(0)) {
			return;
		}
		entityItem.addValue(event.getTime());
		checkMissionFinish(entityItem, cfg);
	}

}
