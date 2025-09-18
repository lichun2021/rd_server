package com.hawk.game.service.mssion.type;

import java.util.List;

import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.MissionService;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventResourceCollectCount;
import com.hawk.game.util.GsConst.MissionFunType;

/**
 * 资源采集数量任务
 * 
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_RESOURCE_COLLECT_COUNT)
public class ResourceCollectCountMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventResourceCollectCount event = (EventResourceCollectCount) missionEvent;

		int resourceType = event.getResourceType();
		List<Integer> conditions = cfg.getIds();

		// conditions为空 或 为0 代表任意的
		if (!conditions.isEmpty() && !conditions.contains(0) && !conditions.contains(resourceType)) {
			return;
		}

		entityItem.addValue(event.getCount());
		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		EventResourceCollectCount event = (EventResourceCollectCount) missionEvent;
		if (event.getCount() > 0) {
			MissionService.getInstance().missionRefreshAsync(player, MissionFunType.FUN_RESOURCE_COLLECT_NUMBER, event.getResourceType(), event.getCount());
		}
	}

}
