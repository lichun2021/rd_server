package com.hawk.game.service.mssion.type;

import java.util.List;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.config.BuildingCfg;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.service.MissionService;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventBuildingCreate;
import com.hawk.game.util.GsConst.MissionFunType;

/**
 * 建筑建造任务
 * 
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_BUILD_CREATE)
public class BuildingCreateMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventBuildingCreate event = (EventBuildingCreate) missionEvent;
		
		BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, event.getBuildingCfgId());
		// 获取建筑基础类型
		int buildType = buildingCfg.getBuildType();
		List<Integer> conditions = cfg.getIds();

		// conditions为空 或 为0 代表任意的
		if (!conditions.isEmpty() && !conditions.contains(0) && !conditions.contains(buildType)) {
			return;
		}

		int count = 0;
		for (int condition : conditions) {
			List<BuildingBaseEntity> list = playerData.getBuildingListByType(BuildingType.valueOf(condition));
			count += list.size();
		}
		entityItem.setValue(count);
		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public void initMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {
		List<Integer> conditions = cfg.getIds();
		for (int condition : conditions) {
			List<BuildingBaseEntity> list = playerData.getBuildingListByType(BuildingType.valueOf(condition));
			entityItem.addValue(list.size());
		}
		checkMissionFinish(entityItem, cfg);
	}

	/**
	 * MissionFunType.FUN_BUILD_NUMBER
	 */
	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		EventBuildingCreate event = (EventBuildingCreate) missionEvent;
		MissionService.getInstance().missionRefreshAsync(player, MissionFunType.FUN_BUILD_NUMBER, event.getBuildingCfgId(), 1);
	}
}
