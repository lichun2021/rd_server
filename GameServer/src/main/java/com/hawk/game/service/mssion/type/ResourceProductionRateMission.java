package com.hawk.game.service.mssion.type;

import java.util.List;

import com.hawk.game.config.MissionCfg;
import com.hawk.game.entity.MissionEntity;
import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.MissionService;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventResourceProductionRate;
import com.hawk.game.util.GsConst.MissionFunType;

/**
 * 资源生产任务
 * 
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_RESOURCE_PRODUCTION_RATE)
public class ResourceProductionRateMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventResourceProductionRate event = (EventResourceProductionRate) missionEvent;

		int resourceType = event.getResourceType();
		List<Integer> conditions = cfg.getIds();

		// conditions为空 或 为0 代表任意的
		if (!conditions.isEmpty() && !conditions.contains(0) && !conditions.contains(resourceType)) {
			return;
		}

		int afterNum = event.getAfterNum();
		if (afterNum > entityItem.getValue()) {
			entityItem.setValue(afterNum);
		}

		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public void initMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {
		List<Integer> conditions = cfg.getIds();
		for (int condition : conditions) {
			int value = (int) playerData.getResourceOutputRate(condition);
			if (value >= entityItem.getValue()) {
				entityItem.setValue(value);
			}
		}

		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		EventResourceProductionRate event = (EventResourceProductionRate) missionEvent;
		
		// 特殊任务，触发任务的地方可能是兵减少的情况，所以算增量的方式跟其它一般任务有所不同
		int typeId = MissionCfg.getTypeId(MissionFunType.FUN_RESOURCE_RATE, event.getResourceType());
		MissionEntity mission = player.getData().getMissionByTypeId(typeId);
		int beforeCount = mission == null ? 0 : mission.getNum();
		int afterCount = (int) player.getData().getResourceOutputRate(event.getResourceType());
		int count = afterCount - beforeCount;
		if (count > 0) {
			MissionService.getInstance().missionRefreshAsync(player, MissionFunType.FUN_RESOURCE_RATE, 
					event.getResourceType(), count);
		}
	}
}
