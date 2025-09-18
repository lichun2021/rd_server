package com.hawk.game.service.mssion.type;

import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventOccupyPresidentTower;

@Mission(missionType = MissionType.OCCUPY_PRESIDENT_TOWER_MINUTE)
public class OccupyPresidentTowerMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		
	}

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventOccupyPresidentTower event = (EventOccupyPresidentTower)missionEvent;
		long occupyTime = event.getOccupyTime();
		if (occupyTime <= 0L) {
			return;
		}
		int minute = (int)(occupyTime / 1000);
		entityItem.addValue(minute);
		checkMissionFinish(entityItem, cfg);
	}
}