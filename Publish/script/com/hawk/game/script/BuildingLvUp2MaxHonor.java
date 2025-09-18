package com.hawk.game.script;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
import com.hawk.game.msg.SuperSoldierTriggeTaskMsg;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.module.agency.PlayerAgencyModule;
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
import com.hawk.log.Action;
import com.hawk.log.Source;
import com.hawk.log.LogConst.PowerChangeReason;

/**
 * 一键勋章满级
 * 
 * http://localhost:8080/script/buildingUpToMaxHonor?playerId=1abj-39b7et-1&level=
 * 
 * @author lating
 *
 */
public class BuildingLvUp2MaxHonor extends HawkScript {
	
	private static final int MAX_LEVEL = 50;
	
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		if (!GsConfig.getInstance().isDebug()) {
			return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "is not debug");
		}
		
		try {
			
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "player not exist");
			}

			int honorLevel = 50;
			if (params.get("level") != null){
				int limitLevel = Integer.parseInt(params.get("level"));
				if (limitLevel > 0 && limitLevel < honorLevel){
					honorLevel = limitLevel;
				}
			}

			unlockArea(player);
			
			for (int buildType : getBuildTypeList()) {
				// 这个时只在前端显示的假建筑，后端屏蔽不处理
				if (buildType == 2213) {
					continue;
				}
				BuildingBaseEntity buildingEntity = getBuildingBaseEntity(player, buildType);
				if (buildingEntity == null && !BuildAreaCfg.isShareBlockBuildType(buildType)) {
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
				
				buildUpgrade(player, buildingEntity, honorLevel);
			}
			
			for (BuildingBaseEntity entity : player.getData().getBuildingEntities()) {
				BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, entity.getBuildingCfgId());
				if (buildingCfg == null || buildingCfg.getLevel() >= MAX_LEVEL) {
					continue;
				}
				
				buildUpgrade(player, entity, honorLevel);
			}
			
			player.refreshPowerElectric(PowerChangeReason.BUILD_LVUP);
			return HawkScript.successResponse("ok");
		} catch (Exception e) {
			HawkException.catchException(e);
			return HawkException.formatStackMsg(e);
		}
	}
	
	/**
	 * 升级建筑
	 * 
	 * @param player
	 * @param buildingEntity
	 */
	private void buildUpgrade(Player player, BuildingBaseEntity buildingEntity,int honorLevel) {
		// 建筑满级
		BuildingCfg oldBuildCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
		BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, oldBuildCfg.getPostStage());
		int flag = 0;
		while (buildingCfg != null && buildingCfg.getLevel() <= honorLevel && flag < honorLevel && buildingCfg.getBuildType() == buildingEntity.getType()) {
			flag = buildingCfg.getLevel();
			BuildingService.getInstance().buildingUpgrade(player, buildingEntity, BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY);
			oldBuildCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
			buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, oldBuildCfg.getPostStage());
		}
	}
	
	/**
	 * 根据建筑cfgId获取建筑实体
	 * @param id
	 */
	public BuildingBaseEntity getBuildingBaseEntity(Player player, int buildingType) {
		Optional<BuildingBaseEntity> op = player.getData().getBuildingEntities().stream()
				.filter(e -> e.getStatus() != BuildingStatus.BUILDING_CREATING_VALUE)
				.filter(e -> e.getType() == buildingType)
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
	private List<Integer> getBuildTypeList() {
		List<Integer> retList = new ArrayList<>();
		
		ConfigIterator<BuildingCfg> buildCfgIterator = HawkConfigManager.getInstance().getConfigIterator(BuildingCfg.class);
		while (buildCfgIterator.hasNext()) {
			BuildingCfg buildCfg = buildCfgIterator.next();
			if (buildCfg.getLevel() > 1) {
				continue;
			}
			BuildLimitCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BuildLimitCfg.class, buildCfg.getLimitType());
			if (cfg == null || cfg.getLimit(MAX_LEVEL) > 1) {
				continue;
			}
			retList.add(buildCfg.getBuildType());
		}
		return retList;
	}
	
	/**
	 * 解锁地块
	 * @param player
	 */
	private void unlockArea(Player player) {
		try {
			Set<Integer> unlockedAreas = player.getData().getPlayerBaseEntity().getUnlockedAreaSet();
			ConfigIterator<BuildAreaCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(BuildAreaCfg.class);
			
			List<Integer> areaList = new ArrayList<Integer>();
			while (iterator.hasNext()) {
				BuildAreaCfg areaCfg = iterator.next();
				int areaId = areaCfg.getId();
				if (unlockedAreas.contains(areaId)) {
					continue;
				}
				
				areaList.add(areaId);
			}
			
			areaList.stream().forEach(e -> {
				player.unlockArea(e);
				MissionManager.getInstance().postMsg(player, new EventUnlockGround(e));
				MissionManager.getInstance().postSuperSoldierTaskMsg(player, new SuperSoldierTriggeTaskMsg(SupersoldierTaskType.UNLOCK_AREA_TASK, 1));
				// 解锁地块任务
				BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.BUIDING_AREA_UNLOCK, Params.valueOf("buildAreaId", e));
			});
			
			player.getPush().synUnlockedArea();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
}
