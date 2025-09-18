package com.hawk.game.service.mssion.type;

import java.util.List;
import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventWorldCollectStart;

@Mission(missionType = MissionType.WORLD_COLLECT_START)
public class WorldCollectStartMission implements IMission {
	
	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventWorldCollectStart event = (EventWorldCollectStart) missionEvent;
		
		List<Integer> conditions = cfg.getIds();
		if (conditions.get(0) != 0 && !conditions.contains(event.getResType())) {
			return;
		}
		
		entityItem.addValue(1);
		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		
	}
	
}
