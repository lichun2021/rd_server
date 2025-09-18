package com.hawk.game.service.mssion.type;

import com.hawk.game.entity.QueueEntity;
import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.protocol.Const.QueueType;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventBuildUpDoing;

@Mission(missionType = MissionType.BUILD_UPLEVEL_DOING)
public class BuildUpLevelDoingMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		
	}

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		int buildType = cfg.getIds().get(0);
		
		EventBuildUpDoing event = (EventBuildUpDoing) missionEvent;
		if (event.getBuildId() / 100 != buildType) {
			return;
		}
		
		int currLevel = playerData.getBuildingMaxLevel(buildType);
		entityItem.setValue(currLevel);
		
		QueueEntity queue = playerData.getQueueByBuildingType(buildType);
		if (queue != null && queue.getQueueType() == QueueType.BUILDING_QUEUE_VALUE) {
			entityItem.setValue(currLevel + 1);
		}
		
		checkMissionFinish(entityItem, cfg);
	}
	
	public void initMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {
		int buildType = cfg.getIds().get(0);
		
		int currLevel = playerData.getBuildingMaxLevel(buildType);
		entityItem.setValue(currLevel);
		
		QueueEntity queue = playerData.getQueueByBuildingType(buildType);
		if (queue != null && queue.getQueueType() == QueueType.BUILDING_QUEUE_VALUE) {
			entityItem.setValue(currLevel + 1);
		}
		
		checkMissionFinish(entityItem, cfg);
	}
}