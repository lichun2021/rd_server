package com.hawk.robot.action.army;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hawk.annotation.RobotAction;
import org.hawk.config.HawkConfigManager;
import org.hawk.enums.EnumUtil;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;

import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.RobotLog;
import com.hawk.robot.config.BattleSoldierCfg;
import com.hawk.robot.config.BuildingCfg;
import com.hawk.robot.util.ClientUtil;
import com.hawk.game.protocol.Army.ArmyInfoPB;
import com.hawk.game.protocol.Army.ArmySoldierPB;
import com.hawk.game.protocol.Army.HPAddSoldierReq;
import com.hawk.game.protocol.Army.HPCollectSoldierReq;
import com.hawk.game.protocol.Army.HPCureSoldierReq;
import com.hawk.game.protocol.Army.HPFireSoldierReq;
import com.hawk.game.protocol.Building.BuildingPB;
import com.hawk.game.protocol.Const.QueueType;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Queue.QueuePB;

/**
 * 
 * 兵种相关操作类
 * 
 * @author lating
 *
 */
@RobotAction(valid = true)
public class PlayerArmyAction extends HawkRobotAction {
	
	/**
	 * 操作类型
	 */
	private static enum ArmyOperType {
		SOLDIER_TRAIN_1,  // 士兵训练，士兵训练有多个的原因，是让该操作随机到的概率更大
		SOLDIER_TRAIN_2,  // 士兵训练
		SOLDIER_TRAIN_3,  // 士兵训练
		SOLDIER_TRAIN_4,  // 士兵训练
		SOLDIER_TRAIN_5,  // 士兵训练
		SOLDIER_TRAIN_6,  // 士兵训练
		SOLDIER_CURE_1,   // 治疗伤兵
		SOLDIER_CURE_2,   // 治疗伤兵
		COLLECT_CURED_SOLDIER,    // 领取治疗完成的伤兵
		COLLECT_TRAINED_SOLDIER,  // 领取训练完成的伤兵
		SOLDIER_FIRE       // 解雇士兵
	}
	
	@Override
	public void doAction(HawkRobotEntity robotEntity) {
		GameRobotEntity robot = (GameRobotEntity) robotEntity;
		ArmyOperType type = EnumUtil.random(ArmyOperType.class);
		switch (type) {
		case SOLDIER_CURE_1:
		case SOLDIER_CURE_2:
			cureSoldier(robot);
			break;
		case COLLECT_CURED_SOLDIER:
			collectCuredSoldier(robot);
			break;
		case COLLECT_TRAINED_SOLDIER:
			collectTrainedSoldier(robot, null);
			break;
		case SOLDIER_FIRE:
			fireSoldier(robot);
			break;
		default:
			trainSoldier(robot, 0);
			break;
		}
		
	}
	
	/**
	 * 士兵训练
	 * 
	 * @param robot
	 * @param armyId
	 */
	public static void trainSoldier(GameRobotEntity robot, int armyId) {
		if (!ClientUtil.isExecuteAllowed(robot, PlayerArmyAction.class.getSimpleName(), 60000)) {
			return;
		}
		
		List<BuildingPB> buildObjList = robot.getBuildingObjects();
		List<String> upgradingBuidings = robot.getCityData().getUpgradingBuildings();
		int trainUplimit = 0;
		String buildingId = "";
		boolean immediate = false;
		if (armyId > 0) {
			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
			Optional<BuildingPB> op = buildObjList.stream().filter(e -> e.getBuildCfgId() / 100 == cfg.getBuilding()).findAny();
			if (!op.isPresent()) {
				return;
			}
			
			immediate = true;
			BuildingPB building = op.get();
			if (upgradingBuidings.contains(building.getId())) {
				return;
			}
			
			BuildingCfg buildCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, building.getBuildCfgId());
			if (!buildCfg.getUnlockedSoldierIds().contains(armyId)) {
				return;
			}
			
			BuildingCfg buidingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, building.getBuildCfgId());
			Optional<QueuePB> queueOptional = robot.getQueueObjects().stream()
		                .filter(queue -> queue.getQueueType() == QueueType.SOILDER_QUEUE || queue.getQueueType() == QueueType.SOLDIER_ADVANCE_QUEUE)
		                .filter(queue -> queue.getInfo() == String.valueOf(buidingCfg.getBuildType()))
		                .findAny();
			if (queueOptional.isPresent()) {
				return;
			}
			 
			trainUplimit = buidingCfg.getTrainQuantity();
			buildingId = building.getId();
			
		} else {
			Collections.shuffle(buildObjList);
			for(BuildingPB building : buildObjList) {
				if (upgradingBuidings.contains(building.getId())) {
					continue;
				}
				
				BuildingCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, building.getBuildCfgId());
				if(cfg == null || !cfg.isArmyProduceBuilding()) {
					continue;
				}
				
				Optional<QueuePB> queueOptional = robot.getQueueObjects().stream()
		                .filter(queue -> queue.getQueueType() == QueueType.SOILDER_QUEUE || queue.getQueueType() == QueueType.SOLDIER_ADVANCE_QUEUE)
		                .filter(queue -> queue.getInfo() == String.valueOf(cfg.getBuildType()))
		                .findAny();
				if (queueOptional.isPresent()) {
					continue;
				}
				
				armyId = BattleSoldierCfg.randSoldierIdByBuildType(cfg);
				if(armyId > 0) {
					trainUplimit = cfg.getTrainQuantity();
					buildingId = building.getId();
					break;
				}
			}
			
			if(armyId <= 0) {
				return;
			}
		}
		
		BuildingPB building = robot.getCityData().getBuildingObjects().get(buildingId);
		if (sendCollectTrainedProtocol(robot, building, robot.getArmyObjects())) {
			return;
		}
		
		// 造兵数量
		int count = HawkRand.randInt(trainUplimit > 10 ? 10 : trainUplimit, trainUplimit);
		if (count <= 0) {
			count = 10;
		}
		
		sendTrainProtocol(robot, armyId, count, buildingId, immediate);
	}
	
	private static void sendTrainProtocol(GameRobotEntity robot, int armyId, int count, String buildingId, boolean immediate) {
		HPAddSoldierReq.Builder builder = HPAddSoldierReq.newBuilder();
		builder.setArmyId(armyId);
		builder.setSoldierCount(count);
		builder.setBuildingUUID(buildingId);
		int randInt = HawkRand.randInt(100);
		if (!immediate && randInt > 50) {
			immediate = true;
		}

		builder.setIsImmediate(immediate);
		builder.setUseGold(randInt < 20);
		// 新手特殊处理标识
		builder.setFlag(0);
		
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.ADD_SOLDIER_C_VALUE, builder));
		robot.getCityData().getLastExecuteTime().put(PlayerArmyAction.class.getSimpleName(), HawkTime.getMillisecond());
		RobotLog.cityPrintln("soldier train action, playerId: {}, buildingId: {}, armyId: {}, count: {}, immediate: {}, useGold: {}", 
				robot.getPlayerId(), buildingId, armyId, count, immediate, randInt < 20);
	}
	
	/**
	 * 领取训练完成的士兵
	 * 
	 * @param robot
	 * @param buildingId
	 * @return
	 */
	public static synchronized boolean collectTrainedSoldier(GameRobotEntity robot, String buildingId) {
		
		List<BuildingPB> buildingObjs = robot.getBuildingObjects();
		if(buildingObjs.size() <= 0) {
			return false;
		}
		
		List<BuildingPB> buildingList = buildingObjs.stream().filter(e -> e.getStatus() == Const.BuildingStatus.SOILDER_HARVEST).collect(Collectors.toList());
		if (buildingList.isEmpty()) {
			return false;
		}
		
		List<ArmyInfoPB> armys = robot.getArmyObjects();
		if (!HawkOSOperator.isEmptyString(buildingId)) {
			return sendCollectTrainedProtocol(robot, robot.getCityData().getBuildingObjects().get(buildingId), armys);
		}
		
		for (BuildingPB building : buildingList) {
			if (sendCollectTrainedProtocol(robot, building, armys)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static synchronized boolean sendCollectTrainedProtocol(GameRobotEntity robot, BuildingPB building, List<ArmyInfoPB> armys) {
	
		for (ArmyInfoPB armyInfo : armys) {
			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyInfo.getArmyId());
			if (armyInfo.getFinishTrainCount() <= 0 || cfg.getBuilding() != building.getBuildCfgId() / 100) {
				continue;
			}
			
			HPCollectSoldierReq.Builder builder = HPCollectSoldierReq.newBuilder();
			builder.setBuildingUUID(building.getId());
			robot.sendProtocol(HawkProtocol.valueOf(HP.code.COLLECT_SOLDIER_C_VALUE, builder));
			RobotLog.cityPrintln("collect soldier action after train, playerId: {}, buildingId: {}", robot.getPlayerId(), building.getId());
			return true;
		}
		
		return false;
	}
	
	/**
	 * 治疗伤兵
	 * 
	 * @param robot
	 */
	public static synchronized void cureSoldier(GameRobotEntity robot) {
		if (collectCuredSoldier(robot)) {
			return;
		}
		
		Optional<QueuePB> op = robot.getQueueObjects().stream().filter(e -> e.getQueueType() == QueueType.CURE_QUEUE).findAny();
		if (op.isPresent()) {
			return;
		}
		
		List<ArmyInfoPB> armyInfos = robot.getArmyObjects();
		if(armyInfos.size() <= 0) {
			return;
		}
		
		sendCureProtocol(robot, armyInfos);
		
	}
	
	private static void sendCureProtocol(GameRobotEntity robot, List<ArmyInfoPB> armyInfos) {
		HPCureSoldierReq.Builder builder = HPCureSoldierReq.newBuilder();
		boolean hasWounded = false;
		for(ArmyInfoPB armyInfo :armyInfos) {
			if (armyInfo.getCureFinishCount() > 0) {
				return;
			}
			
			if(armyInfo.getWoundedCount() <= 0) {
				continue;
			}
			hasWounded = true;
			ArmySoldierPB.Builder soldier = ArmySoldierPB.newBuilder();
			soldier.setArmyId(armyInfo.getArmyId());
			soldier.setCount(HawkRand.randInt(1, armyInfo.getWoundedCount()));
			builder.addSoldiers(soldier);
		}
		
		if(!hasWounded) {
			return;
		}
		
		// 医务室的uuid，现在不用了
		builder.setBuildingUUID("abc");
		// 是否立即治疗
		boolean immediate = robot.getPuidNum() % 2 == 0;
		builder.setIsImmediate(immediate);
		boolean useGold = false;
		if(!immediate) {
			builder.setUseGold(useGold = HawkRand.randPercentRate(50));
		}
		
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.CURE_SOLDIER_C_VALUE, builder));
		
		RobotLog.cityPrintln("soldier cure action, playerId: {}, immddiate: {}, useGold: {}", robot.getPlayerId(), immediate, useGold);
	}
	
	/**
	 * 领取治疗完成的伤兵
	 * 
	 * @param robot
	 * @return
	 */
	public static synchronized boolean collectCuredSoldier(GameRobotEntity robot) {
		Optional<QueuePB> queueOp = robot.getQueueObjects().stream().filter(e -> e.getQueueType() == QueueType.CURE_QUEUE).findAny();
		if (queueOp.isPresent()) {
			return false;
		}
		
		List<BuildingPB> buildingObjs = robot.getBuildingByType(Const.BuildingType.HOSPITAL_STATION_VALUE);
		if(buildingObjs.size() <= 0) {
			return false;
		}
		
		Optional<BuildingPB> op = buildingObjs.stream().filter(e -> e.getStatus() == Const.BuildingStatus.CURE_FINISH_HARVEST).findAny();
		if(!op.isPresent()) {
			return false;
		}

		List<ArmyInfoPB> armys = robot.getArmyObjects();
		for (ArmyInfoPB armyInfo : armys) {
			if (armyInfo.getCureFinishCount() <= 0) {
				continue;
			}
			
			robot.sendProtocol(HawkProtocol.valueOf(HP.code.COLLECT_CURE_FINISH_SOLDIER_VALUE));
			RobotLog.cityPrintln("collect soldier action afte cure, playerId: {}", robot.getPlayerId());
			return true;
		}
		
		return false;
	}
	
	/**
	 * 解雇士兵
	 * 
	 * @param gameRobotEntity
	 */
	private void fireSoldier(GameRobotEntity gameRobotEntity) {
		List<ArmyInfoPB> armyInfos = gameRobotEntity.getArmyObjects();
		if(armyInfos.size() <= 0) {
			return;
		}
		
		HPFireSoldierReq.Builder builder = HPFireSoldierReq.newBuilder();
		Map<Integer, Integer> fireArmy = new HashMap<Integer, Integer>();
		boolean isFreeSoldier = HawkRand.randPercentRate(50);
		
		for(ArmyInfoPB armyInfo :armyInfos) {
			int count = isFreeSoldier ? armyInfo.getFreeCount() : armyInfo.getWoundedCount();
			if(count <= 0) {
				continue;
			}
			ArmySoldierPB.Builder soldier = ArmySoldierPB.newBuilder();
			soldier.setArmyId(armyInfo.getArmyId());
			count = HawkRand.randInt(count);
			soldier.setCount(count);
			fireArmy.put(armyInfo.getArmyId(), count);
			builder.addSoldiers(soldier);
		}
		
		builder.setIsWounded(!isFreeSoldier);
		gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.FIRE_SOLDIER_C_VALUE, builder));
		
		RobotLog.cityPrintln("fire soldier, playerId: {}, isWounded: {}, army: {}", gameRobotEntity.getPlayerId(), !isFreeSoldier, fireArmy);
	}
}
