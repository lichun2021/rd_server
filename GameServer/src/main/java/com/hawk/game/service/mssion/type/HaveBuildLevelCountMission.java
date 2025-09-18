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
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventBuildingCreate;
import com.hawk.game.service.mssion.event.EventBuildingUpgrade;

/**
 * 拥有{1}类型{1}等级以上建筑{2}个  (需要初始化)
 * @author golden
 * @version 2018/01/10
 */
@Mission(missionType = MissionType.MISSION_HAVE_BUILD_LEVEL_COUNT)
public class HaveBuildLevelCountMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {

	}

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		if (missionEvent instanceof EventBuildingCreate) {
			createRefresh(playerData, missionEvent, entityItem, cfg);
		}

		if (missionEvent instanceof EventBuildingUpgrade) {
			upgradeRefresh(playerData, missionEvent, entityItem, cfg);
		}
	}

	private <T extends MissionEvent> void createRefresh(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventBuildingCreate event = (EventBuildingCreate) missionEvent;
		BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, event.getBuildingCfgId());
		// 获取建筑基础类型
		int buildType = buildingCfg.getBuildType();
		if (cfg.getIds().get(0) != 0 && buildType != cfg.getIds().get(0) ) {
			return;
		}
		refreshMission(playerData, entityItem, cfg);
	}
	
	private <T extends MissionEvent> void upgradeRefresh(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventBuildingUpgrade event = (EventBuildingUpgrade) missionEvent;
		
		BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, event.getBuildingCfgId());
		// 获取建筑基础类型
		int buildType = buildingCfg.getBuildType();
		if (cfg.getIds().get(0) != 0 && buildType != cfg.getIds().get(0)) {
			return;
		}
		refreshMission(playerData, entityItem, cfg);
	}
	
	@Override
	public void initMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {
		refreshMission(playerData, entityItem, cfg);
	}

	private void refreshMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {
		List<Integer> conditions = cfg.getIds();
		int conditionId = conditions.get(0);
		int conditionLvl = conditions.get(1);
		
		List<BuildingBaseEntity> builds = playerData.getBuildingListByType(BuildingType.valueOf(conditionId));
		if (conditionId == 0) {
			builds =  playerData.getBuildingEntities();
		}
		
		int count = 0;
		for (BuildingBaseEntity build : builds) {
			BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, build.getBuildingCfgId());
			if (buildingCfg.getLevel() < conditionLvl) {
				continue;
			}
			
			count++;
		}
		entityItem.setValue(count);
		checkMissionFinish(entityItem, cfg);
	}
}
