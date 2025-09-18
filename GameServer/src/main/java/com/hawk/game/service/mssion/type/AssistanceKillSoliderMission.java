package com.hawk.game.service.mssion.type;

import java.util.Map;

import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventPvpBattle;

@Mission(missionType = MissionType.ASSISTANCE_KILL_SOLIDER)
public class AssistanceKillSoliderMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		
	}

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventPvpBattle event = (EventPvpBattle)missionEvent;
		if (event.getMarchType() != WorldMarchType.ATTACK_PLAYER && event.getMarchType() != WorldMarchType.MASS) {
			return;
		}
		if (event.isLeader() || event.isAttacker()) {
			return;
		}
		int killCount = 0;
		Map<Integer, Integer> armyKillMap = event.getArmyKillMap();
		for (int count : armyKillMap.values()) {
			killCount += count;
		}
		entityItem.addValue(killCount);
		checkMissionFinish(entityItem, cfg);
	}
}