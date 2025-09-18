package com.hawk.game.service.mssion.type;

import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventAgency;

@Mission(missionType = MissionType.MISSION_AGENCY_TIMES)
public class AgencyTimesMission implements IMission {
	
	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		if (cfg.getIds().isEmpty() || cfg.getIds().get(0) == 0) {
			entityItem.addValue(1);
		} else {
			EventAgency event = (EventAgency)missionEvent;
			if (event.getEventId() == cfg.getIds().get(0)) {
				entityItem.addValue(1);	
			}
		}
		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		
	}
}
