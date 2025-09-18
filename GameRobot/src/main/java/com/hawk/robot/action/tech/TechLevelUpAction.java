package com.hawk.robot.action.tech;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hawk.annotation.RobotAction;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;

import com.hawk.game.protocol.Building.BuildingPB;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.QueueType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Queue.QueuePB;
import com.hawk.game.protocol.Technology.LevelUpTechnologyReq;
import com.hawk.game.protocol.Technology.LvlUpType;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.RobotLog;
import com.hawk.robot.config.TechnologyCfg;
import com.hawk.robot.util.ClientUtil;

/**
 * 
 * 科技研究
 * 
 * @author lating
 *
 */
@RobotAction(valid = true)
public class TechLevelUpAction extends HawkRobotAction {

	@Override
	public void doAction(HawkRobotEntity robotEntity) {
		GameRobotEntity robot = (GameRobotEntity) robotEntity;
		List<BuildingPB> buildings = robot.getBuildingByType(BuildingType.FIGHTING_LABORATORY_VALUE);
		if (buildings.isEmpty()) {
			return;
		}
		
		Optional<Integer> techId = robot.getCityData().getUnlockedTechs().stream().findAny();
		if(techId.isPresent()){
			techLevelUp(robot, techId.get());
		}
	}
	
	public static void techLevelUpAfterBuilding(GameRobotEntity robot, int buildId) {
		List<BuildingPB> buildings = robot.getBuildingByType(BuildingType.FIGHTING_LABORATORY_VALUE);
		if (buildings.isEmpty()) {
			return;
		}
		
		List<Integer> techIds = TechnologyCfg.getUnlockTechByBuildId(buildId);
		if(techIds == null || techIds.size() <= 0) {
			return;
		}
		
		List<Integer> unlockedTechIds = techIds.stream().filter(e -> robot.getCityData().checkTechPreCondition(e)).collect(Collectors.toList());
		if(unlockedTechIds.isEmpty()) {
			return;
		}
		
		techLevelUp(robot, unlockedTechIds.get(0));
	}
	
	public static void techLevelUpAfterTech(GameRobotEntity robot, int techId) {
		List<BuildingPB> buildings = robot.getBuildingByType(BuildingType.FIGHTING_LABORATORY_VALUE);
		if (buildings.isEmpty()) {
			return;
		}
		
		List<Integer> techIds = TechnologyCfg.getUnlockTechByTechId(techId);
		if(techIds == null || techIds.size() <= 0) {
			return;
		}
		
		List<Integer> unlockedTechIds = techIds.stream().filter(e -> robot.getCityData().checkTechPreCondition(e)).collect(Collectors.toList());
		if(unlockedTechIds.isEmpty()) {
			return;
		}
		
		techLevelUp(robot, unlockedTechIds.get(0));
	}
	
	private static void techLevelUp(GameRobotEntity robot, int techId) {
		if (!ClientUtil.isExecuteAllowed(robot, TechLevelUpAction.class.getSimpleName(), 60000)) {
			return;
		}
		
		int maxLevel = TechnologyCfg.getMaxLevelByTechId(techId/100);
		if (techId % 100 > maxLevel) {
			return;
		}
		
		Optional<Integer> op = robot.getCityData().getTechIds().stream().filter(e -> e/100 == techId/100 && e >= techId).findAny();
		if (op.isPresent()) {
			return;
		}
		
		// 判断科技研究所建筑是否正在研究中(升级中也可以进行研究)。。。。
		List<QueuePB> queueList = robot.getQueueObjects();
		for (QueuePB queue : queueList) {
			if (queue.getQueueType() == QueueType.SCIENCE_QUEUE && queue.getEndTime() > HawkTime.getMillisecond()) {
				return;
			}
		}

		LevelUpTechnologyReq.Builder builder = LevelUpTechnologyReq.newBuilder();
		builder.setTechId(techId);
		LvlUpType type = LvlUpType.NORMAL;
		if (HawkRand.randPercentRate(66)) {
			type = LvlUpType.BUY_RES;
			if (HawkRand.randPercentRate(50)) {
				type = LvlUpType.BUY_RES_AND_TIME;
			}
		}
		
		builder.setType(type);
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.TECHNOLOGY_UPLEVEL_C_VALUE, builder));
		robot.getCityData().getLastExecuteTime().put(TechLevelUpAction.class.getSimpleName(), HawkTime.getMillisecond());
		RobotLog.cityPrintln("tech level up action, playerId: {}, techId: {}, lvlUpType: {}", robot.getPlayerId(), techId, type.name());
	}
}
