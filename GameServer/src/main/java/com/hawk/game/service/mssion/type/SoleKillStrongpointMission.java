package com.hawk.game.service.mssion.type;

import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.PlayerAchieveService;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventAttackStrongpoint;

@Mission(missionType = MissionType.MISSION_SOLE_STRONGPOINT_WIN_TIMES)
public class SoleKillStrongpointMission implements IMission{

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		
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
		boolean soleAchieveConclude = PlayerAchieveService.getInstance().soleAchieveConclude(playerData.getPlayerId(), MissionType.MISSION_SOLE_STRONGPOINT_WIN_TIMES.intValue());
		if (!soleAchieveConclude) {
			return;
		}
		
		entityItem.addValue(1);
		checkMissionFinish(entityItem, cfg);
	}

}

