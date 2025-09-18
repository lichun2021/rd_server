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
import com.hawk.game.service.mssion.event.EventMechaAdvance;

@Mission(missionType = MissionType.MECHA_ADVANCE)
public class MechaAdvanceMission implements IMission {
	
	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventMechaAdvance event = (EventMechaAdvance) missionEvent;
		List<Integer> conditions = cfg.getIds();
		if (conditions.get(0) != 0 && !conditions.contains(event.getSoldierId())) {
			return;
		}
		
		long value = entityItem.getValue() + 1;
		entityItem.setValue(value);
		checkMissionFinish(entityItem, cfg);
	}

	public void initMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {
		int ssId = cfg.getIds().get(0);
		Optional<SuperSoldierEntity> optional = playerData.getSuperSoldierEntityList().stream().filter(e -> e.getSoldierId() == ssId).findAny();
		
		int value = 0;
		if (optional.isPresent()) {
			value = optional.get().getStep() == cfg.getValue() ? 1: 0;
		}
		
		entityItem.setValue(value);
		checkMissionFinish(entityItem, cfg);
	}
	
	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		
	}
	
}
