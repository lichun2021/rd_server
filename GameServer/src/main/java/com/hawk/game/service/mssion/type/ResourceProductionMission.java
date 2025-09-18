package com.hawk.game.service.mssion.type;

import java.util.List;

import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventResourceProduction;

/**
 * 城内收集{1}资源{2}点
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_RESOURCE_PRODUCTION)
public class ResourceProductionMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		
	}

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventResourceProduction event = (EventResourceProduction) missionEvent;

		int resourceType = event.getResType();
		List<Integer> conditions = cfg.getIds();

		// conditions为空 或 为0 代表任意的
		if (!conditions.isEmpty() && !conditions.contains(0) && !conditions.contains(resourceType)) {
			return;
		}
		entityItem.addValue(event.getAddNum());
		checkMissionFinish(entityItem, cfg);
	}
}
