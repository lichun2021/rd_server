package com.hawk.game.service.mssion.type;

import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventAttackFoggy;

/**
 * 集结攻打要塞n次
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_MASS_ATK_FOGGY_WIN_TIMES)
public class AttackFoggyMassWinTimes implements IMission {

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		
	}

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventAttackFoggy event = (EventAttackFoggy) missionEvent;
		if (!event.isMass()) {
			return;
		}
		if (event.getFoggyLvl() < cfg.getIds().get(0) || !event.isWin()) {
			return;
		}
		entityItem.addValue(1);
		checkMissionFinish(entityItem, cfg);
	}
}
