package com.hawk.game.service.mssion.type;

import java.util.List;
import java.util.Map;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventMechaPartRepair;

@Mission(missionType = MissionType.MECHA_PART_REPAIR)
public class MechaPartRepairMission implements IMission {
	
	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventMechaPartRepair event = (EventMechaPartRepair) missionEvent;
		
		List<Integer> conditions = cfg.getIds();
		if (conditions.get(0) != 0 && !conditions.contains(event.getPartId())) {
			return;
		}
		
		entityItem.setValue(1);
		checkMissionFinish(entityItem, cfg);
	}

	public void initMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {
		int partId = cfg.getIds().get(0);
		Map<Integer, Integer> map = RedisProxy.getInstance().getSupersoldierTaskInfo(playerData.getPlayerId());
		int value = 0;
		if (partId > 0) {
			value = map.getOrDefault(partId, 0) < 0 ? 1 : 0;
		} else {
			value = (int) map.values().stream().filter(e -> e < 0).count();
		}
		
		entityItem.setValue(value);
		checkMissionFinish(entityItem, cfg);
	}
	
	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		
	}
	
}
