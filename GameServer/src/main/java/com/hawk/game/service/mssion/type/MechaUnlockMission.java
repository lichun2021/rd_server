package com.hawk.game.service.mssion.type;

import java.util.List;
import java.util.Optional;
import com.hawk.game.entity.SuperSoldierEntity;
import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventMechaUnlock;

@Mission(missionType = MissionType.MECHA_UNLOCK)
public class MechaUnlockMission implements IMission {
	
	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventMechaUnlock event = (EventMechaUnlock) missionEvent;
		
		List<Integer> conditions = cfg.getIds();
		if (conditions.get(0) != 0 && !conditions.contains(event.getSoldierId())) {
			return;
		}
		
		entityItem.setValue(1);
		checkMissionFinish(entityItem, cfg);
	}

	public void initMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {
		int ssId = cfg.getIds().get(0);
		List<SuperSoldierEntity> supersoldiers = playerData.getSuperSoldierEntityList();
		int value = supersoldiers.size();
		if (ssId > 0) {
			Optional<SuperSoldierEntity> optional = supersoldiers.stream().filter(e -> e.getSoldierId() == ssId).findAny();
			value = optional.isPresent() ? 1 : 0;
		}
		
		entityItem.setValue(value > 0 ? 1 : 0);
		checkMissionFinish(entityItem, cfg);
	}
	
	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		
	}
	
}
