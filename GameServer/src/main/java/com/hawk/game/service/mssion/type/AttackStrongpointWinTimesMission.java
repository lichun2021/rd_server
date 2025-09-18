package com.hawk.game.service.mssion.type;

import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.MissionService;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventAttackStrongpoint;
import com.hawk.game.util.GsConst.MissionFunType;

@Mission(missionType = MissionType.MISSION_ATTACK_STRONGPOINT_WIN_TIMES)
public class AttackStrongpointWinTimesMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		EventAttackStrongpoint event = (EventAttackStrongpoint)missionEvent;
		if (!event.isWin()) {
			return;
		}
		
		MissionService.getInstance().missionRefreshAsync(player, MissionFunType.FUN_ATTACK_STRONGPOINT_WIN, 0, 1);
		MissionService.getInstance().missionRefreshAsync(player, MissionFunType.FUN_ATTACK_STRONGPOINT_WIN, event.getLevel(), 1);
	}

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventAttackStrongpoint event = (EventAttackStrongpoint)missionEvent;
		if (!event.isWin()) {
			return;
		}
		if (event.getLevel() < cfg.getIds().get(0)) {
			return;
		}
		entityItem.addValue(1);
		checkMissionFinish(entityItem, cfg);
	}
}