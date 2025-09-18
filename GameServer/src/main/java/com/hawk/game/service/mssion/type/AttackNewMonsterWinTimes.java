package com.hawk.game.service.mssion.type;

import java.util.List;

import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventAttackNewMonster;

/**
 * 击杀{1}等级的新版野怪{2}次
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_ATTACK_NEW_MONSTER_WIN)
public class AttackNewMonsterWinTimes implements IMission {

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
	}

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventAttackNewMonster event = (EventAttackNewMonster) missionEvent;
		if (!event.isWin()) {
			return;
		}
		// x等级以上
		List<Integer> conditions = cfg.getIds();
		if (event.getMonsterLvl() < conditions.get(0)) {
			return;
		}
		entityItem.addValue(1);
		checkMissionFinish(entityItem, cfg);
	}
}
