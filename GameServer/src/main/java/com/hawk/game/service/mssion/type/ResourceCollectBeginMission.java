package com.hawk.game.service.mssion.type;

import java.util.List;

import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventResourceCollectBegin;

/**
 * 资源开始采集任务(占领x等级以上的资源点)
 * 
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_RESOURCE_COLLECT_BEGIN)
public class ResourceCollectBeginMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventResourceCollectBegin event = (EventResourceCollectBegin) missionEvent;

		int resourceLevel = event.getLevel();
		List<Integer> conditions = cfg.getIds();

		// conditions为空 或 为0 代表任意的
		if (!conditions.isEmpty() && !conditions.contains(0) && resourceLevel < conditions.get(0)) {
			return;
		}

		entityItem.addValue(1);
		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		// TODO Auto-generated method stub
		
	}
}
