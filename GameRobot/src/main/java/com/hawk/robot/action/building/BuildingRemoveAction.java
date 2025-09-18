package com.hawk.robot.action.building;

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
import com.hawk.robot.RobotLog;
import com.hawk.robot.config.BuildAreaCfg;
import com.hawk.robot.config.BuildingCfg;
import com.hawk.robot.util.ClientUtil;
import com.hawk.game.protocol.Building.BuildingPB;
import com.hawk.game.protocol.Building.BuildingRemoveReq;
import com.hawk.game.protocol.Const.BuildingStatus;
import com.hawk.game.protocol.Queue.QueuePB;
import com.hawk.game.protocol.HP;

/**
 * 
 * 移除建筑
 * 
 * @author lating
 *
 */
@RobotAction(valid = true)
public class BuildingRemoveAction extends HawkRobotAction {
	
	@Override
	public void doAction(HawkRobotEntity robotEntity) {
		GameRobotEntity gameRobotEntity = (GameRobotEntity) robotEntity;
		remove(gameRobotEntity);
	}
	
	/**
	 * 移除建筑
	 * @param robot
	 * @param buildType
	 */
	public static synchronized void remove(GameRobotEntity robot) {
		if (!ClientUtil.isExecuteAllowed(robot, BuildingRemoveAction.class.getSimpleName(), 60000)) {
			return;
		}
		
		List<BuildingPB> buildList = robot.getBuildingObjects();
		if(buildList.isEmpty()) {
			return;
		}
		
		BuildingPB buildPB= null;
		for(BuildingPB build : buildList) {
			if (!BuildAreaCfg.isSharaBlockBuildType(build.getBuildCfgId()/100)) {
				continue;
			}
			
			if(build.getStatus() == BuildingStatus.COMMON && checkBuildingStatus(robot, build)) {
				buildPB = build;
				break;
			}
		}
		
		if(buildPB == null) {
			return;
		}
		
		boolean immediate = HawkRand.randPercentRate(50);
		BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildPB.getBuildCfgId());
		if(!immediate && !robot.getBasicData().hasFreeBuildingQueue(buildingCfg)) {
			return;
		}
		BuildingRemoveReq.Builder builder = BuildingRemoveReq.newBuilder();
		builder.setUuid(buildPB.getId());
		builder.setImmediately(immediate);
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.BUILDING_REMOVE_C_VALUE, builder));
		robot.getCityData().getLastExecuteTime().put(BuildingRemoveAction.class.getSimpleName(), HawkTime.getMillisecond());
		RobotLog.cityDebugPrintln("building remove action, playerId: {}, buildId: {}, immediate: {}", robot.getPlayerId(), buildPB.getId(), immediate);
	}
	
	private static boolean checkBuildingStatus(GameRobotEntity robot, BuildingPB buildPB) {
		Optional<QueuePB> op = robot.getQueueObjects().stream().filter(e -> e.getItemId().equals(buildPB.getId())).findAny();
		return !op.isPresent();
	}
	
}
