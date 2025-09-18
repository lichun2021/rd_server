package com.hawk.game.service.mssion.type;

import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventGrabResource;

/**
 * 抢夺{1}资源{2}点
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_GRAB_RESOURCE_COUNT)
public class GrabResourceMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		
	}

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventGrabResource event = (EventGrabResource)missionEvent;
		int conditionType = cfg.getIds().get(0);
		if (conditionType != 0 && conditionType != event.getResourceType()) {
			return;
		}
		entityItem.addValue(event.getCount());
		checkMissionFinish(entityItem, cfg);
	}
}
