package com.hawk.game.service.mssion.type;

import java.util.List;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.config.BuildingCfg;
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
import com.hawk.game.service.mssion.event.EventBuildingCreate;
import com.hawk.game.service.mssion.event.EventBuildingUpgrade;
import com.hawk.game.util.GsConst.MissionFunType;

/**
 * 建筑升级任务
 * 
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_BUILD_UPGRADE)
public class BuildingUpgradeMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		// 建造触发建筑升级
		if (missionEvent instanceof EventBuildingCreate) {
			createRefresh(missionEvent, entityItem, cfg);
		}

		// 建筑升级触发
		if (missionEvent instanceof EventBuildingUpgrade) {
			upgradeRefresh(missionEvent, entityItem, cfg);
		}
	}

	// 建筑建造刷新建筑等级
	private <T extends MissionEvent> void createRefresh(T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventBuildingCreate event = (EventBuildingCreate) missionEvent;
		
		BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, event.getBuildingCfgId());
		// 获取建筑基础类型
		int buildType =  buildingCfg.getBuildType();
		List<Integer> conditions = cfg.getIds();

		// conditions为空 或 为0 代表任意的
		if (!conditions.isEmpty() && !conditions.contains(0) && !conditions.contains(buildType)) {
			return;
		}

		// 升级后等级
		if (entityItem.getValue() <= 0) {
			entityItem.setValue(1);
		}

		checkMissionFinish(entityItem, cfg);
	}

	// 建筑升级刷新建筑等级
	private <T extends MissionEvent> void upgradeRefresh(T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventBuildingUpgrade event = (EventBuildingUpgrade) missionEvent;
		
		BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, event.getBuildingCfgId());
		// 获取建筑基础类型
		int buildType = buildingCfg.getBuildType();
		List<Integer> conditions = cfg.getIds();

		// conditions为空 或 为0 代表任意的
		if (!conditions.isEmpty() && !conditions.contains(0) && !conditions.contains(buildType)) {
			return;
		}

		// 升级后等级
		int afterLevel = event.getAfterLevel();
		if (afterLevel > entityItem.getValue()) {
			entityItem.setValue(afterLevel);
		}

		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public void initMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {
		List<Integer> conditions = cfg.getIds();
		for (int condition : conditions) {
			int level = playerData.getBuildingMaxLevel(condition);
			if (level > entityItem.getValue()) {
				entityItem.setValue(level);
			}
		}

		checkMissionFinish(entityItem, cfg);
	}
	
	/**
	 * MissionFunType.FUN_BUILD_LEVEL
	 */
	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		if (missionEvent instanceof EventBuildingCreate) {
			EventBuildingCreate event = (EventBuildingCreate) missionEvent;
			MissionService.getInstance().refreshBuildLevelConditionMission(player, event.getBuildingCfgId());
			missionTrigger(player, event.getBuildingCfgId());
		}

		if (missionEvent instanceof EventBuildingUpgrade) {
			EventBuildingUpgrade event = (EventBuildingUpgrade) missionEvent;
			MissionService.getInstance().refreshBuildLevelConditionMission(player, event.getBuildingCfgId());
			missionTrigger(player, event.getBuildingCfgId());
			MissionService.getInstance().missionRefreshAsync(player, MissionFunType.FUN_BUILD_NUMBER, event.getBuildingCfgId(), 1);
		}
		
	}
	
	private void missionTrigger(Player player, int buildCfgId) {
		BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildCfgId);
		int funId = buildingCfg.getBuildType();
		int maxLevel = player.getData().getBuildingMaxLevel(funId);
		
		int typeId = MissionCfg.getTypeId(MissionFunType.FUN_BUILD_LEVEL, funId);
		MissionEntity mission = player.getData().getMissionByTypeId(typeId);
		int beforeCount = mission == null ? 0 : mission.getNum();
		
		int count = maxLevel - beforeCount;
		if (count > 0) {
			MissionService.getInstance().missionRefreshAsync(player, MissionFunType.FUN_BUILD_LEVEL, funId, 1);
		}
	}
}
