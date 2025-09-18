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
import com.hawk.game.util.GsConst.MissionFunType;
import com.hawk.game.service.mssion.event.EventAttackNewMonster;

/**
 * 击杀{1}等级的新版野怪{2}次
 * 
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_ATTACK_NEW_MONSTER)
public class AttackNewMonsterTimes implements IMission {

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		EventAttackNewMonster event = (EventAttackNewMonster) missionEvent;
		MissionService.getInstance().missionRefreshAsync(player, MissionFunType.FUN_ATTACK_NEW_MONSTER, 0, event.getPracAtkTimes());
		MissionService.getInstance().missionRefreshAsync(player, MissionFunType.FUN_ATTACK_NEW_MONSTER, event.getMonsterLvl(), event.getPracAtkTimes());
	}

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventAttackNewMonster event = (EventAttackNewMonster) missionEvent;
		int lvl = event.getMonsterLvl();

		List<Integer> conditions = cfg.getIds();
		// conditions为空 或 为0 代表任意的
		if (!conditions.isEmpty() && !conditions.contains(0) && !conditions.contains(lvl)) {
			return;
		}
		entityItem.addValue(event.getPracAtkTimes());
		checkMissionFinish(entityItem, cfg);
	}
}
