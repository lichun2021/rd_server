package com.hawk.game.service.mssion.type;

import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.MissionService;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventMonsterAttack;
import com.hawk.game.util.GsConst.MissionFunType;

/**
 * 攻打野怪任务
 * 
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_MONSTER_ATTACK)
public class MonsterAttackMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventMonsterAttack event = (EventMonsterAttack) missionEvent;

		int monsterLvl = event.getLevel();
		int conditionLvl = cfg.getIds().get(0);

		// conditions为空 或 为0 代表任意的
		if (conditionLvl != 0 && monsterLvl < conditionLvl) {
			return;
		}

		entityItem.addValue(event.getAtkTimes());
		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		EventMonsterAttack event = (EventMonsterAttack) missionEvent;
		
		MissionService.getInstance().missionRefreshAsync(player, MissionFunType.FUN_ATTACK_MONSTER, event.getLevel(), event.getAtkTimes());
		MissionService.getInstance().missionRefreshAsync(player, MissionFunType.FUN_ATTACK_MONSTER, 0, event.getAtkTimes()); // 统计所有等级的怪
		if(event.isWin()) {
			MissionService.getInstance().missionRefreshAsync(player, MissionFunType.FUN_ATTACK_MONSTER_WIN, event.getLevel(), event.getAtkTimes());
			MissionService.getInstance().missionRefreshAsync(player, MissionFunType.FUN_ATTACK_MONSTER_WIN, 0, event.getAtkTimes()); // 统计所有等级的怪
		}
		
	}
}
