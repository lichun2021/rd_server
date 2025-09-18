package com.hawk.game.service.mssion.type;

import java.util.List;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.ArmyService;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventPvpBattle;

@Mission(missionType = MissionType.TRAP_KILL_SOLIDER)
public class TrapKillSoliderMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		
	}

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventPvpBattle event = (EventPvpBattle)missionEvent;
		if (event.getMarchType() != WorldMarchType.ATTACK_PLAYER && event.getMarchType() != WorldMarchType.MASS) {
			return;
		}
		
		if (event.isAttacker()) {
			return;
		}
		
		int killCount = 0;
		List<ArmyInfo> selfArmy = event.getSelfArmy();
		for (ArmyInfo army : selfArmy) {
			int armyId = army.getArmyId();
			int count = army.getKillCount();
			
			BattleSoldierCfg battleSoldierCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
			if (!ArmyService.getInstance().isTrap(battleSoldierCfg.getType())) {
				continue;
			}
			killCount += count;
		}
		entityItem.addValue(killCount);
		checkMissionFinish(entityItem, cfg);
	}
}
