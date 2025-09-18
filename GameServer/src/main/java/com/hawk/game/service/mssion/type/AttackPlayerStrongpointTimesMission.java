package com.hawk.game.service.mssion.type;

import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventAttackPlayerStrongpoint;

/**
 * 攻击玩家据点并胜利{2}次
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_ATTACK_PLAYER_STRONGPOINT_TIMES)
public class AttackPlayerStrongpointTimesMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		
	}

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventAttackPlayerStrongpoint event = (EventAttackPlayerStrongpoint)missionEvent;
		if (!event.isWin()) {
			return;
		}
		entityItem.addValue(1);
		checkMissionFinish(entityItem, cfg);
	}

}
