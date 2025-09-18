package com.hawk.robot.data;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;

import com.hawk.game.protocol.Army.ArmyChangeCause;
import com.hawk.game.protocol.Army.ArmyInfoPB;
import com.hawk.game.protocol.Army.HPArmyInfoSync;
import com.hawk.game.protocol.Building.BuildingPB;
import com.hawk.game.protocol.Building.CityDefPB;
import com.hawk.game.protocol.Building.PushBuildingStatus;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.BuildingStatus;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.LimitType;
import com.hawk.game.protocol.HP;
import com.hawk.robot.GameRobotApp;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.RobotLog;
import com.hawk.robot.action.army.PlayerArmyAction;
import com.hawk.robot.action.building.BuildingOutFireAction;
import com.hawk.robot.config.BattleSoldierCfg;
import com.hawk.robot.config.BuildLimitCfg;
import com.hawk.robot.config.BuildingCfg;
import com.hawk.robot.config.TechnologyCfg;

public class CityData {
	/**
	 * 机器人信息(上层数据)
	 */
	private GameRobotData robotData;
	/**
	 * 大本等级
	 */
	private int constructionBuildLevel = 0;
	/**
	 * 城防信息
	 */
	protected CityDefPB cityDefInfo = null;
	/**
	 * 已解锁的区块信息
	 */
	protected List<Integer> unlockedAreas = new CopyOnWriteArrayList<>();
	/**
	 * 已解锁但还未建造的建筑(数量)
	 */
	protected Map<Integer, Integer> unLockBuildingMap = new ConcurrentHashMap<>();
	/**
	 * 建筑升级依赖的还未建造出来的前置建筑
	 */
	protected List<Integer> unlockedNeedCreateBuilds = new CopyOnWriteArrayList<>();
	/**
	 * 阻碍大本建筑升级的前置建筑
	 */
	protected Map<String, BuildingPB> hinderUpgradeBuidings = new ConcurrentHashMap<>();
	
	/**
	 * 建筑对象列表
	 */
	protected Map<String, BuildingPB> buildingObjects = new ConcurrentHashMap<String, BuildingPB>();
	/**
	 * 正在升级的建筑
	 */
	protected List<String> upgradingBuildings = new CopyOnWriteArrayList<String>();
	/**
	 * 已发送服务器领取奖励的任务
	 */
	protected List<String> sendMissions = new CopyOnWriteArrayList<String>();
	
	/**
	 * 资源建筑最后一次收取资源的时间
	 */
	protected Map<String, Long> resBuildingCollectTimeMap = new ConcurrentHashMap<>(); 
	/**
	 * 科技数据
	 */
	protected Set<Integer> techIds = new HashSet<>();
	/**
	 * 已满足研究条件还未升级的科技
	 */
	protected Set<Integer> unlockedTechs = new HashSet<>();
	/**
	 * 兵种对象列表
	 */
	protected Map<String, ArmyInfoPB> armyObjects = new ConcurrentHashMap<String, ArmyInfoPB>();
	/**
	 * action执行的时间
	 */
	protected Map<String, Long> lastExecuteTime = new ConcurrentHashMap<>();
	
	protected List<String> usedSharedBlocks = new CopyOnWriteArrayList<>();
	
	/////////////////////////////////////////////////////////////////////////////////
	
	public CityData(GameRobotData gameRobotData) {
		robotData = gameRobotData;
	}
	
	public GameRobotData getRobotData() {
		return robotData;
	}

	public Map<String, BuildingPB> getBuildingObjects() {
		return buildingObjects;
	}
	
	public Map<Integer, Integer> getUnLockBuildingMap() {
		return unLockBuildingMap;
	}
	
	public void refreshResBuildingCollectTime(String buildingUuid) {
		resBuildingCollectTimeMap.put(buildingUuid, HawkTime.getMillisecond());
	}
	
	public void addUnlockedNeedCreateBuild(int buildCfgId) {
		if (!unlockedNeedCreateBuilds.contains(buildCfgId)) {
			unlockedNeedCreateBuilds.add(buildCfgId);
		}
	}
	
	public void removeUnlockedNeedCreateBuild(int buildCfgId) {
		if (unlockedNeedCreateBuilds.contains(buildCfgId)) {
			unlockedNeedCreateBuilds.remove(Integer.valueOf(buildCfgId));
		}
	}
	
	public List<Integer> getUnlockedNeedCreateBuild() {
		return unlockedNeedCreateBuilds;
	}
	
	public void addHinderUpgradeBuiding(BuildingPB building) {
		hinderUpgradeBuidings.put(building.getId(), building);
	}
	
	public void removeHinderUpgradeBuiding(BuildingPB building) {
		hinderUpgradeBuidings.remove(building.getId());
	}
	
	public List<BuildingPB> getHinderUpgradeBuidings() {
		return new ArrayList<>(hinderUpgradeBuidings.values());
	}
	
	/**
	 * 移除建筑
	 * @param buildingId
	 */
	public void removeBuilding(String buildingId) {
		BuildingPB build = buildingObjects.get(buildingId);
		if (build != null) {
			getUsedSharedBlocks().remove(build.getIndex());
		} else {
			RobotLog.cityPrintln("remove building blocks failed, buildingId: {}", buildingId);
		}
		buildingObjects.remove(buildingId);
		hinderUpgradeBuidings.remove(buildingId);
	}
	
	public int getConstructionBuildLevel() {
		return constructionBuildLevel;
	}

	public void setConstructionBuildLevel(int constructionBuildLevel) {
		this.constructionBuildLevel = constructionBuildLevel;
	}
	
	public void updateCityDef(CityDefPB cityDefInfo) {
		this.cityDefInfo = cityDefInfo;
	}
	
	public CityDefPB getCityDefInfo() {
		return this.cityDefInfo;
	}

	public List<Integer> getUnlockedAreas() {
		return unlockedAreas;
	}

	public void setUnlockedAreas(List<Integer> unlockedAreas) {
		this.unlockedAreas.clear();
		this.unlockedAreas.addAll(unlockedAreas);
	}
	
	public Set<Integer> getUnlockedTechs() {
		return unlockedTechs;
	}
	
	public Set<Integer> getTechIds() {
		return techIds;
	}
	
	/**
	 * 刷新科技数据
	 * @param techCfgId
	 */
	public void addTechIds(List<Integer> techCfgIds) {
		techIds.addAll(techCfgIds);
		unlockedTechs.removeAll(techCfgIds);
	}
	
	/**
	 * 通过建筑解锁科技
	 * @param buildId
	 */
	public void unlockTechByBuilding(Integer... buildIds) {
		for(int id : buildIds) {
			List<Integer> techIds = TechnologyCfg.getUnlockTechByBuildId(id);
			if(techIds == null || techIds.size() <= 0) {
				return;
			}
			List<Integer> unlockedTechIds = techIds.stream().filter(e -> checkTechPreCondition(e)).collect(Collectors.toList());
			unlockedTechs.addAll(unlockedTechIds);
		}
		unlockedTechs.removeAll(techIds);
		
	}
	
	/**
	 * 通过科技解锁科技
	 * @param techId
	 */
	public void unlockTechByTech(List<Integer> techList) {
		for(int techId : techList){
			List<Integer> techIds = TechnologyCfg.getUnlockTechByTechId(techId);
			if(techIds == null || techIds.size() <= 0) {
				return;
			}
			List<Integer> unlockedTechIds = techIds.stream().filter(e -> checkTechPreCondition(e)).collect(Collectors.toList());
			unlockedTechs.addAll(unlockedTechIds);
		}
		unlockedTechs.removeAll(techIds);
	}
	
	/**
	 * 判断科技研究的前置条件是否已满足
	 * @param techId
	 * @return
	 */
	public boolean checkTechPreCondition(int techId) {
		TechnologyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(TechnologyCfg.class, techId);
		List<Integer> preBuildings = cfg.getConditionBuildList();
		// 前置建筑判断
		List<Integer> buildings = getBuildingObjects().values().stream().map(e -> e.getBuildCfgId()).collect(Collectors.toList());
		if (!preBuildings.isEmpty()) {
			for (int conCfgId : preBuildings) {
				int conType = conCfgId / 100;
				int conLvl = conCfgId % 100;
				boolean match = false;
				for (int buildCfgId : buildings) {
					if (buildCfgId / 100 == conType && buildCfgId % 100 >= conLvl) {
						match = true;
						break;
					}
				}
				if (!match) {
					return false;
				}
			}
		}
		
		// 前置科技判断
		List<Integer> preTechs = cfg.getConditionTechList();
		List<Integer> techCfgIds = techIds.stream().collect(Collectors.toList());
		if (!preTechs.isEmpty()) {
			for (int conCfgId : preTechs) {
				int conType = conCfgId / 100;
				int conLvl = conCfgId % 100;
				boolean match = false;
				for (int techCfgId : techCfgIds) {
					if (techCfgId / 100 == conType && techCfgId % 100 >= conLvl) {
						match = true;
						break;
					}
				}
				if (!match) {
					return false;
				}
			}
		}

		if (robotData.getBasicData().getPlayerInfo().getVipLevel() < cfg.getFrontVip()) {
			return false;
		}
		return true;
	}
	
	public Map<String, ArmyInfoPB> getArmyObjects() {
		return armyObjects;
	}
	
	/**
	 * 刷新军队数据
	 * @param armyList
	 */
	public void refreshArmyData(GameRobotEntity robotEntity, HPArmyInfoSync armyInfoPB) {
		List<ArmyInfoPB> armyList = armyInfoPB.getArmyInfosList();
		ArmyChangeCause cause = armyInfoPB.getCause();
		if(armyList == null || armyList.isEmpty()) {
			return;
		}
		
		for(ArmyInfoPB armyInfo : armyList) {
			armyObjects.put(armyInfo.getId(), armyInfo);
			checkDoArmyAction(robotEntity, cause, armyInfo);
		}
	}
	
	private void checkDoArmyAction(GameRobotEntity robotEntity, ArmyChangeCause cause, ArmyInfoPB armyInfo) {
		if (cause != ArmyChangeCause.MARCH_BACK && cause != ArmyChangeCause.FIRE 
				&& cause != ArmyChangeCause.TRAIN_CANCEL && cause != ArmyChangeCause.DEFAULT) {
			return;
		}
		
		if (cause != ArmyChangeCause.DEFAULT) {
			if (armyInfo.getFreeCount() > 0) {
				return;
			}
			
			GameRobotApp.getInstance().executeTask(new Runnable() {
				@Override
				public void run() {
					PlayerArmyAction.trainSoldier(robotEntity, armyInfo.getArmyId());
				}
			});
			
			return;
		}
		
		if (armyInfo.getCureFinishCount() > 0) {
			GameRobotApp.getInstance().executeTask(new Runnable() {
				@Override
				public void run() {
					PlayerArmyAction.collectCuredSoldier(robotEntity);
				}
			});
		} else if (armyInfo.getWoundedCount() > 0) {
			// 玩家登录时有可能兵种数据到了，建筑数据还没到，这种情况这里不判断的话会出异常
			List<BuildingPB> buildingPBList = robotEntity.getBuildingByType(BuildingType.HOSPITAL_STATION_VALUE);
			if (!buildingPBList.isEmpty()) {
				BuildingPB building = buildingPBList.get(0);
				if (building.getStatus() == BuildingStatus.COMMON) {
					GameRobotApp.getInstance().executeTask(new Runnable() {
						@Override
						public void run() {
							PlayerArmyAction.cureSoldier(robotEntity);
						}
					});
				}
			}
		}
		
		if (armyInfo.getFinishTrainCount() > 0) {
			GameRobotApp.getInstance().executeTask(new Runnable() {
				@Override
				public void run() {
					BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyInfo.getArmyId());
					BuildingPB building = robotEntity.getBuildingByType(cfg.getBuilding()).get(0);
					PlayerArmyAction.collectTrainedSoldier(robotEntity, building.getId());
				}
			});
		}
	}
	
	/**
	 * 刷新建筑状态
	 * @param buildingStatus
	 */
	public void refreshBuildingStatus(GameRobotEntity robotEntity, PushBuildingStatus buildingStatus) {
        String buildId = buildingStatus.getBuildId();
        BuildingPB building = getBuildingObjects().get(buildId); 
        if(building == null) {
        	RobotLog.cityPrintln("refresh building status, playerId: {}, buildId, building count: {}", robotEntity.getPlayerId(), buildId, getBuildingObjects().size());
        	return;
        }
        
        BuildingStatus status = buildingStatus.getStatus();
        BuildingPB.Builder builder = building.toBuilder().setStatus(status);
        buildingObjects.put(buildId, builder.build());
        
        BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, building.getBuildCfgId());
        int type = buildingCfg.getBuildType();

        // 医务所状态变化
        if (type == Const.BuildingType.HOSPITAL_STATION_VALUE) {
            if (status == BuildingStatus.CURE_FINISH_HARVEST) {
                GameRobotApp.getInstance().executeTask(new Runnable() {
    				@Override
    				public void run() {
    					PlayerArmyAction.collectCuredSoldier(robotEntity);
    				}
    			});
            } else if (status == BuildingStatus.SOLDIER_WOUNDED && HawkRand.randPercentRate(50)) {
                GameRobotApp.getInstance().executeTask(new Runnable() {
    				@Override
    				public void run() {
    					PlayerArmyAction.cureSoldier(robotEntity);
    				}
    			});
            }
        }

        // 造完兵待收兵
        if (status == BuildingStatus.SOILDER_HARVEST) {
            GameRobotApp.getInstance().executeTask(new Runnable() {
 				@Override
 				public void run() {
 					PlayerArmyAction.collectTrainedSoldier(robotEntity, buildId);
 				}
 			});
        }
        
        if(status == BuildingStatus.CITYWALL_ONFIRE_STATUS) {
        	if(robotEntity.getCityData().getCityDefInfo() == null) {
        		robotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.CITYDEF_REQ_C_VALUE));
        	} else {
        		 GameRobotApp.getInstance().executeTask(new Runnable() {
     				@Override
     				public void run() {
     					BuildingOutFireAction.outFire(robotEntity);
     				}
     			});
        	}
        }

	}
	
	/**
	 * 获取资源建筑
	 * @return
	 */
	public BuildingPB getResBuilding() {
		if(resBuildingCollectTimeMap.size() == 0) {
			for(BuildingPB building : getBuildingObjects().values()) {
				BuildingCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, building.getBuildCfgId());
				if(cfg != null && cfg.isResourceBuilding()) {
					resBuildingCollectTimeMap.put(building.getId(), 0L);
				}
			}
		}
		
		long time = Long.MAX_VALUE;
		String uuid = "";
		for(Entry<String, Long> entry : resBuildingCollectTimeMap.entrySet()) {
			if(entry.getValue() < time) {
				time = entry.getValue();
				uuid = entry.getKey();
			}
		}
		if(HawkOSOperator.isEmptyString(uuid)) {
			return null;
		}
		
		resBuildingCollectTimeMap.put(uuid, HawkTime.getMillisecond());
		return getBuildingObjects().get(uuid);
	}
	
	/**
	 * 刷新建筑数据
	 * @param buildingList
	 */
	public void refreshBuildingData(BuildingPB... buildingList) {
		if(buildingList == null || buildingList.length <= 0) {
			return;
		}
		
		int cityLevel = 0;
		for(BuildingPB buildingPB : buildingList) {
			if (!usedSharedBlocks.contains(buildingPB.getIndex())) {
				usedSharedBlocks.add(buildingPB.getIndex());
			}
			
			removeUnlockedNeedCreateBuild(buildingPB.getBuildCfgId());
			removeHinderUpgradeBuiding(buildingPB);
			
			if (buildingPB.getStatus() == BuildingStatus.BUILDING_CREATING) {
				continue;
			}
			
			buildingObjects.put(buildingPB.getId(), buildingPB);
			upgradingBuildings.remove(buildingPB.getId());
			BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingPB.getBuildCfgId());
			if(buildingCfg.getBuildType() == BuildingType.CONSTRUCTION_FACTORY_VALUE) {
				cityLevel = buildingCfg.getLevel();
				setConstructionBuildLevel(cityLevel);
			}
			
			if(buildingCfg.isResourceBuilding() && !resBuildingCollectTimeMap.containsKey(buildingPB.getId())) {
				resBuildingCollectTimeMap.put(buildingPB.getId(), 0L);
			}
		}
		
		if(cityLevel > 0) {
			Map<Integer, Integer> buildLimitCountMap = BuildLimitCfg.getBuildLimitCountMap(cityLevel);
			for(Entry<Integer, Integer> entry : buildLimitCountMap.entrySet()) {
				unLockBuildingMap.put(entry.getKey(), entry.getValue());
			}
		}
	}
	
	public List<String> getUpgradingBuildings() {
		return upgradingBuildings;
	}
	
	public List<String> getSendMissions() {
		return sendMissions;
	}
	
	public Map<String, Long> getLastExecuteTime() {
		return lastExecuteTime;
	}
	
	public List<String> getUsedSharedBlocks() {
		return usedSharedBlocks;
	}
	
	/**
	 * 根据类型获取建筑对象
	 * @param type
	 * @return
	 */
	public List<BuildingPB> getBuildingByType(int type) {
		List<BuildingPB> buildings = new ArrayList<>();
		for(BuildingPB building : buildingObjects.values()) {
			if(building.getBuildCfgId() / 100 == type) {
				buildings.add(building);
			}
		}
		return buildings;
	}
	
	/**
	 * 获取指定类型最大等级的建筑
	 * @param type
	 * @return
	 */
	public BuildingPB getMaxLevelBuilding(int type) {
		int level = 0;
		BuildingPB maxLevelBuilding = null;
		for (BuildingPB building : buildingObjects.values()) {
			BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, building.getBuildCfgId());
			if (buildingCfg == null) {
				continue;
			}
			if (buildingCfg.getBuildType() != type) {
				continue;
			}
			if (buildingCfg.getLevel() > level) {
				level = buildingCfg.getLevel();
				maxLevelBuilding = building;
			}
		}
		
		return maxLevelBuilding;
	}
	
	/**
	 * 通过限制类型获取建筑数据
	 * @param limitTypes
	 * @return
	 */
	public List<BuildingPB> getBuildingListByLimitType(LimitType... limitTypes) {
		EnumSet<LimitType> set = EnumSet.noneOf(LimitType.class);
		for (LimitType e : limitTypes) {
			set.add(e);
		}

		List<BuildingPB> list = new ArrayList<BuildingPB>();
		for (BuildingPB building : buildingObjects.values()) {
			BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, building.getBuildCfgId());
			if (buildingCfg == null) {
				continue;
			}
			LimitType cfgLimitType = LimitType.valueOf(buildingCfg.getLimitType());
			if (set.contains(cfgLimitType)) {
				list.add(building);
			}
		}
		
		return list;
	}
	
}
