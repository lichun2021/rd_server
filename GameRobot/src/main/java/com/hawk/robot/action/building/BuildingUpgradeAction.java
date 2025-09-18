package com.hawk.robot.action.building;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hawk.annotation.RobotAction;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;

import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.RobotAppConfig;
import com.hawk.robot.RobotLog;
import com.hawk.robot.action.army.PlayerArmyAction;
import com.hawk.game.protocol.HP;
import com.hawk.robot.config.BuildingCfg;
import com.hawk.robot.util.ClientUtil;
import com.hawk.game.protocol.Queue.QueuePB;
import com.hawk.game.protocol.Building.BuildingPB;
import com.hawk.game.protocol.Const.BuildingStatus;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.QueueType;
import com.hawk.game.protocol.Building.BuildingUpgradeReq;

/**
 * 
 * 建筑升级，从已建造出的建筑中随机一个建筑，1、判断有没有空闲队列  2、 判断前置条件是否满足
 * 
 * @author lating
 *
 */
@RobotAction(valid = true)
public class BuildingUpgradeAction extends HawkRobotAction {

	@Override
	public void doAction(HawkRobotEntity robotEntity) {
		GameRobotEntity gameRobotEntity = (GameRobotEntity) robotEntity;
		doBuildingUpgradeAction(gameRobotEntity);
	}
	
	public static synchronized void doBuildingUpgradeAction(GameRobotEntity robot) {
		if (!ClientUtil.isExecuteAllowed(robot, BuildingUpgradeAction.class.getSimpleName(), 60000)) {
			return;
		}
		
		if(!robot.isOnline()) {
			return;
		}
		
		//正在升级中的建筑
		List<String> upgradingBuidings = robot.getCityData().getUpgradingBuildings();
		// 待升级的前置建筑
		List<BuildingPB> hinderUpgradeBuildings = robot.getCityData().getHinderUpgradeBuidings();
		// 新出现的阻碍升级的前置建筑
		List<Integer> hinderBuildCfgIdList = new ArrayList<>();
		// 待升级的前置建筑不为空时，先升级他们
		if (hinderUpgradeBuildings.size() > 0) {
			BuildingPB building = getBuilding(robot, hinderUpgradeBuildings, upgradingBuidings, hinderBuildCfgIdList);
			if (building != null) {
				upgradingBuidings.add(building.getId());
				sendProtocol(robot, building, true);
				return;
			}
		} else {
			// 升级大本建筑
			BuildingPB building = robot.getBuildingByType(BuildingType.CONSTRUCTION_FACTORY_VALUE).get(0);
			int cityLevelUpLimit = RobotAppConfig.getInstance().getCityLevelUpLimit();
			// 城堡升到指定等级不让再升了
			if (cityLevelUpLimit > 0 && building.getBuildCfgId() % 100 > cityLevelUpLimit) {
				RobotLog.cityPrintln("city level gt {}, playerId: {}", building.getBuildCfgId() % 100, robot.getPlayerId());
				return;
			}
			
			if (isBuildingUpgradeAllowed(robot, building, upgradingBuidings, hinderBuildCfgIdList)) {
				BuildingCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, building.getBuildCfgId() + 1);
				if (cfg != null) {
					boolean immediate = HawkRand.randPercentRate(75);
					if(!immediate && !robot.getBasicData().hasFreeBuildingQueue(cfg)) {
						RobotLog.cityErrPrintln("building upgrade failed, has no free building queue, playerId: {}, buildingCfgId: {}", robot.getPlayerId(), building.getBuildCfgId());
						return;
					}
					
					upgradingBuidings.add(building.getId());
					sendProtocol(robot, building, immediate);
					return;
				}
			}
		}
		
		// 大本建筑达到最大等级，从已有建筑中随机抽取一个进行升级
		if (hinderBuildCfgIdList.isEmpty()) {
			List<BuildingPB> buildingObjs = robot.getBuildingObjects();
			Collections.shuffle(buildingObjs);
			BuildingPB building = getBuilding(robot, buildingObjs, upgradingBuidings, hinderBuildCfgIdList);
			if(building != null) {
				boolean immediate = HawkRand.randPercentRate(50);
				BuildingCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, building.getBuildCfgId() + 1);
				if(!immediate && !robot.getBasicData().hasFreeBuildingQueue(cfg)) {
					RobotLog.cityErrPrintln("building upgrade failed, has no free building queue, playerId: {}, buildingCfgId: {}", robot.getPlayerId(), building.getBuildCfgId());
					return;
				}
				
				upgradingBuidings.add(building.getId());
				sendProtocol(robot, building, immediate);
			}
			
			return;
		}
		
		
		List<BuildingPB> hinderBuildings = new ArrayList<>();
		for (int cfgId : hinderBuildCfgIdList) {
			BuildingPB maxLevelbuilding = robot.getMaxLevelBuilding(cfgId / 100);
			if (maxLevelbuilding == null) {
				robot.getCityData().addUnlockedNeedCreateBuild(cfgId);
			} else {
				robot.getCityData().addHinderUpgradeBuiding(maxLevelbuilding);
				hinderBuildings.add(maxLevelbuilding);
			}
		}
		
		if (hinderBuildings.isEmpty()) {
			if (!BuildingCreateAction.doBuildingCreateAction(robot)) {
				RobotLog.cityPrintln("building upgrade failed, hinderBuildings not created, playerId: {}", robot.getPlayerId());
			}
			return;
		}
		
		BuildingPB building = getBuilding(robot, hinderBuildings, upgradingBuidings, null);
		if (building == null) {
			RobotLog.cityPrintln("building upgrade failed, find building: {}, playerId: {}", robot.getPlayerId(), building);
			return;
		}
		
		upgradingBuidings.add(building.getId());
		sendProtocol(robot, building, true);
	}
	
	/**
	 * 发送建筑升级协议
	 * @param robot
	 * @param building
	 * @param immediate
	 */
	private static void sendProtocol(GameRobotEntity robot, BuildingPB building, boolean immediate) {
		BuildingUpgradeReq.Builder builder = BuildingUpgradeReq.newBuilder();
		// 建筑uuid
		builder.setId(building.getId());
		// 建筑配置id
		builder.setBuildCfgId(building.getBuildCfgId());
		builder.setImmediately(immediate);
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.BUILDING_UPGRADE_C_VALUE, builder));
		robot.getCityData().getLastExecuteTime().put(BuildingUpgradeAction.class.getSimpleName(), HawkTime.getMillisecond());
		RobotLog.cityPrintln("building upgrade action, playerId: {}, uuid: {}, buildingId: {}, immediate: {}", 
				robot.getPlayerId(), building.getId(), building.getBuildCfgId(), immediate);
	}
	
	public static BuildingPB getBuilding(GameRobotEntity robot, List<BuildingPB> buildingObjs, List<String> upgradingBuidings, List<Integer> hinderBuildCfgIdList) {
		for(BuildingPB build : buildingObjs) {
			if (isBuildingUpgradeAllowed(robot, build, upgradingBuidings, hinderBuildCfgIdList)) {
				return build;
			}
		}
		
		return null;
	}
	
	private static boolean isBuildingUpgradeAllowed(GameRobotEntity robot, BuildingPB build, List<String> upgradingBuidings, List<Integer> hinderBuildCfgIdList) {
		if (upgradingBuidings.contains(build.getId())) {
			RobotLog.cityPrintln("building upgrade action repeated, playerId: {}, uuid: {}, cfgId: {}", robot.getPlayerId(), build.getId(), build.getBuildCfgId());
			return false;
		}
		
		BuildingCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, build.getBuildCfgId());
		if(BuildingCfg.getBuildMaxLevelByType(cfg.getBuildType()) <= cfg.getLevel()) {
			return false;
		}
		
		BuildingStatus status = build.getStatus();
		if ((status == BuildingStatus.SOILDER_HARVEST || status == BuildingStatus.COMMON) && checkBuildingStatus(robot, build)) {
			if (status == BuildingStatus.SOILDER_HARVEST) {
				PlayerArmyAction.sendCollectTrainedProtocol(robot, build, robot.getArmyObjects());
			}
			
			BuildingCfg nextLevelCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, build.getBuildCfgId() + 1);
			if(nextLevelCfg != null && checkFrontBuilding(robot, nextLevelCfg.getFrontBuildIds(), hinderBuildCfgIdList)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean checkFrontBuilding(GameRobotEntity robot, int[] frontIds, List<Integer> hinderBuildCfgIdList) {
		// 不需要前置建筑
		if (frontIds == null) {
			return true;
		}

		// 检查前置建筑
		boolean success = true;
		for (int cfgId : frontIds) {
			BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, cfgId);
			if (buildingCfg == null) {
				continue;
			}

			Optional<BuildingPB> op = robot.getBuildingObjects().stream()
					.filter(e -> e.getBuildCfgId()/100 == buildingCfg.getBuildType())
					.filter(e -> e.getBuildCfgId() >= cfgId)
					.findAny();

			if (!op.isPresent()) {
				if (hinderBuildCfgIdList == null) {
					return false;
				}
				hinderBuildCfgIdList.add(cfgId);
				success = false;
			}
		}
		
		return success;
	}
	
	private static boolean checkBuildingStatus(GameRobotEntity robot, BuildingPB buildPB) {
		// 一个建筑，建筑队列和功能队列可共存
		Optional<QueuePB> op = robot.getQueueObjects().stream().filter(
				e -> (e.getItemId().equals(buildPB.getId()) && e.getQueueType() == QueueType.BUILDING_QUEUE)).findAny();
		
		return !op.isPresent();
	}
	
}
