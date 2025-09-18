package com.hawk.game.script;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.GsConfig;
import com.hawk.game.config.BuildAreaCfg;
import com.hawk.game.config.BuildLimitCfg;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.module.agency.PlayerAgencyModule;
import com.hawk.game.msg.SuperSoldierTriggeTaskMsg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Building.BuildingUpdateOperation;
import com.hawk.game.protocol.Const.BuildingStatus;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.SuperSoldier.SupersoldierTaskType;
import com.hawk.game.service.BuildingService;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventUnlockGround;
import com.hawk.game.util.GsConst;
import com.hawk.log.LogConst.PowerChangeReason;

/**
 * 建筑一键满级
 * http://localhost:8080/script/buildingUpGradeToMax?playerName=?level=
 * @author golden
 *
 */
public class BuildingUpGradeToMax extends HawkScript {
	
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		return doAction(params);
	}
	
	public static String doAction(Map<String, String> params) {
		if (!GsConfig.getInstance().isDebug()) {
			return HawkScript.failedResponse(SCRIPT_ERROR, "");
		}
		
		try {
			if (!GsConfig.getInstance().isDebug()) {
				return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "is not debug");
			}
			
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "player not exist");
			}
			
			unlockArea(player);
			
			AtomicInteger indexObj = new AtomicInteger(1);
			for (int buildType : getBuildTypeSet()) {
				// 这个时只在前端显示的假建筑，后端屏蔽不处理
				if (buildType == 2213) {
					continue;
				}
				
				if (buildType == BuildingType.PRISM_TOWER_VALUE) {
					specialBuildLevelUp(player);
					specialBuildLevelUp(player);
					continue;
				}
				
				if (BuildAreaCfg.isShareBlockBuildType(buildType)) {
					unlockShareBlockBuild(player, buildType, indexObj);
					continue;
				}
				
				BuildingBaseEntity buildingEntity = getBuildingBaseEntity(player, buildType);
				if (buildingEntity == null) {
					BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, (buildType * 100) + 1);
					buildingEntity = player.getData().createBuildingEntity(buildingCfg, "1", false);
					BuildingService.getInstance().createBuildingFinish(player, buildingEntity, BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY, HP.code.BUILDING_CREATE_PUSH_VALUE);
					if (buildType == BuildingType.RADAR_VALUE) {
						try {
							PlayerAgencyModule module = player.getModule(GsConst.ModuleType.AGENCY_MODULE);
							module.initData();
						} catch (Exception e) {
						}
					}
				}
				
				BuildingCfg oldBuildCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
				BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, oldBuildCfg.getPostStage());
				while (buildingCfg != null && oldBuildCfg.getLevel() < 30 && buildingCfg.getBuildType() == buildType) {
					BuildingService.getInstance().buildingUpgrade(player, buildingEntity, BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY);
					oldBuildCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
					buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, oldBuildCfg.getPostStage());
				}
			}
			player.refreshPowerElectric(PowerChangeReason.BUILD_LVUP);
			return HawkScript.successResponse("ok");
		} catch (Exception e) {
			HawkException.catchException(e);
			return HawkException.formatStackMsg(e);
		}
	}
	
	private static void specialBuildLevelUp(Player player) {
		BuildingBaseEntity buildingEntity = getBuildingBaseEntity(player, BuildingType.PRISM_TOWER_VALUE);
		if (buildingEntity == null) {
			BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, (BuildingType.PRISM_TOWER_VALUE * 100) + 1);
			buildingEntity = player.getData().createBuildingEntity(buildingCfg, "1", false);
			BuildingService.getInstance().createBuildingFinish(player, buildingEntity, BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY, HP.code.BUILDING_CREATE_PUSH_VALUE);
		} else {
			BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, (BuildingType.PRISM_TOWER_VALUE * 100) + 1);
			buildingEntity = player.getData().createBuildingEntity(buildingCfg, "2", false);
			BuildingService.getInstance().createBuildingFinish(player, buildingEntity, BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY, HP.code.BUILDING_CREATE_PUSH_VALUE);
		}
		
		BuildingCfg oldBuildCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
		BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, oldBuildCfg.getPostStage());
		while (buildingCfg != null && oldBuildCfg.getLevel() < 30 && buildingCfg.getBuildType() == BuildingType.PRISM_TOWER_VALUE) {
			BuildingService.getInstance().buildingUpgrade(player, buildingEntity, BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY);
			oldBuildCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
			buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, oldBuildCfg.getPostStage());
		}
	}
	
	private static void unlockShareBlockBuild(Player player, int buildType, AtomicInteger indexObj) {
		int count = player.getData().getBuildCount(BuildingType.valueOf(buildType));
		while (count < 5) {
			BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, (buildType * 100) + 1);
			BuildingBaseEntity buildingEntity = player.getData().createBuildingEntity(buildingCfg, String.valueOf(indexObj.get()), false);
			indexObj.addAndGet(1);
			count++;
			BuildingService.getInstance().createBuildingFinish(player, buildingEntity, BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY, HP.code.BUILDING_CREATE_PUSH_VALUE);
		}
		
		List<BuildingBaseEntity> buildingList = player.getData().getBuildingListByType(BuildingType.valueOf(buildType));
		for (BuildingBaseEntity buildingEntity : buildingList) {
			BuildingCfg oldBuildCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
			BuildingCfg newBuildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, oldBuildCfg.getPostStage());
			while (newBuildingCfg != null && oldBuildCfg.getLevel() < 30 && newBuildingCfg.getBuildType() == buildType) {
				BuildingService.getInstance().buildingUpgrade(player, buildingEntity, BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY);
				oldBuildCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
				newBuildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, oldBuildCfg.getPostStage());
			}
		}
	}
	
	private static void unlockArea(Player player) {
		try {
			Set<Integer> unlockedAreas = player.getData().getPlayerBaseEntity().getUnlockedAreaSet();
			ConfigIterator<BuildAreaCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(BuildAreaCfg.class);
			while (iterator.hasNext()) {
				BuildAreaCfg cfg = iterator.next();
				int areaId = cfg.getId();
				if (unlockedAreas.contains(areaId)) {
					continue;
				}
				
				player.unlockArea(areaId);			
				// 解锁地块任务
				MissionManager.getInstance().postMsg(player, new EventUnlockGround(areaId));
				MissionManager.getInstance().postSuperSoldierTaskMsg(player, new SuperSoldierTriggeTaskMsg(SupersoldierTaskType.UNLOCK_AREA_TASK, 1));
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		// 推送建筑信息
		player.getPush().synUnlockedArea();
	}
	
	/**
	 * 根据建筑cfgId获取建筑实体
	 * @param id
	 */
	public static BuildingBaseEntity getBuildingBaseEntity(Player player, int buildCfgId) {
		Optional<BuildingBaseEntity> op = player.getData().getBuildingEntities().stream()
				.filter(e -> e.getStatus() != BuildingStatus.BUILDING_CREATING_VALUE)
				.filter(e -> e.getBuildingCfgId() / 100 == buildCfgId)
				.findAny();
		if(op.isPresent()) {
			return op.get();
		}
		return null;
	}
	
	
	/**
	 * 获取需要升级至满级的建筑列表
	 * @return
	 */
	private static Set<Integer> getBuildTypeSet() {
		Set<Integer> set = new HashSet<>();
		
		ConfigIterator<BuildingCfg> buildCfgIterator = HawkConfigManager.getInstance().getConfigIterator(BuildingCfg.class);
		while (buildCfgIterator.hasNext()) {
			BuildingCfg buildCfg = buildCfgIterator.next();
			if (buildCfg.getLevel() > 1) {
				continue;
			}
			BuildLimitCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BuildLimitCfg.class, buildCfg.getLimitType());
			if (cfg == null) {
				continue;
			}
			set.add(buildCfg.getBuildType());
		}
		return set;
	}
}
